package stateHandlers

import Client
import ClientsRepository
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import keyboards.PaymentKeyboard
import keyboards.SelectDayKeyboard
import keyboards.SelectHoursKeyboard

abstract class CompletingInterviewHandler(
    private val clientsRepository: ClientsRepository,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val vkApiClient: VKApiClient,
    private val qiwiApiClient: QiwiApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    protected suspend fun askHours(client: Client) {
        vkApiClient.sendMessageSafely(
            client.id,
            "Сколько часов Вы бы хотели тренироваться на этой неделе?" ,
            SelectHoursKeyboard().keyboard
        )
    }

    protected suspend fun askDay(client: Client) {
        vkApiClient.sendMessageSafely(
            client.id,
            "В какой день у Вас будет старт?" ,
            SelectDayKeyboard().keyboard
        )
    }

    protected suspend fun checkIfTrial(client: Client) {
        if (client.trialPeriodEnded) {
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_PAYMENT
            )
            vkApiClient.sendMessageSafely(
                client.id,
                "Тренировочный план составлен!\nОсталось только оплатить месячную подписку, и Вы сможете приступить к тренировкам!\n" +
                        "Чтобы открыть окно с оплатой, нажмите \"Оплатить подписку\". " +
                        "После совершения платежа нажмите \"Подтвердить оплату\".",
                keyboard = PaymentKeyboard(qiwiApiClient.getPayUrl(client.billId)).keyboard
            )
        } else {
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_START
            )
            vkApiClient.sendMessageSafely(
                client.id,
                "Тренировочный план составлен!\n" +
                        "Чтобы получить его и начать недельный цикл, нажмите \"Начать цикл\"."
            )
        }
    }
}