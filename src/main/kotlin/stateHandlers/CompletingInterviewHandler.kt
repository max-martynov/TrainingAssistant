package stateHandlers

import client.Client
import ClientsRepository
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import client.Status
import keyboards.PaymentKeyboard
import keyboards.SelectDayKeyboard
import keyboards.SelectHoursKeyboard
import keyboards.StartKeyboard

abstract class CompletingInterviewHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
    private val qiwiApiClient: QiwiApiClient
) : StateHandler() {

    protected suspend fun askHours(client: Client) {
        vkApiClient.sendMessageSafely(
            client.id,
            "Сколько часов Вы бы хотели тренироваться на этой неделе?" ,
            SelectHoursKeyboard().getKeyboard()
        )
    }

    protected suspend fun askDay(client: Client) {
        vkApiClient.sendMessageSafely(
            client.id,
            "В какой день у Вас будет старт?" ,
            SelectDayKeyboard().getKeyboard()
        )
    }

    protected suspend fun checkIfTrial(client: Client) {
        if (client.trialPeriodEnded) {
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_PAYMENT
            )
            qiwiApiClient.updateBill(client, clientsRepository)
            vkApiClient.sendMessageSafely(
                client.id,
                "Тренировочный план составлен!\nОсталось только оплатить месячную подписку, и Вы сможете приступить к тренировкам!\n" +
                        "Чтобы открыть окно с оплатой, нажмите \"Оплатить подписку\". Оплата возможно только с помощью банковской карты, но не волнуйтесь, Ваши данные в безопасности.\n" +
                        "После совершения платежа нажмите \"Подтвердить оплату\"!",
                keyboard = PaymentKeyboard().getKeyboard(qiwiApiClient.getPayUrl(client.billId))
            )
        } else {
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_START
            )
            vkApiClient.sendMessageSafely(
                client.id,
                "Тренировочный план составлен!\n" +
                        "Чтобы получить его и начать тренироваться, нажмите на кпоку ниже.",
                keyboard = StartKeyboard().getKeyboard()
            )
        }
    }
}