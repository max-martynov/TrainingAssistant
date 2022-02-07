package client

import TrainingPlan

enum class Status {
    NEW_CLIENT,
    WAITING_FOR_PLAN,
    WAITING_FOR_START,
    ACTIVE,
    COMPLETING_INTERVIEW0,
    COMPLETING_INTERVIEW1,
    COMPLETING_INTERVIEW2,
    COMPLETING_INTERVIEW3,
    COMPLETING_INTERVIEW4,
    WAITING_FOR_PAYMENT,
}


data class Client(
    val id: Int,
    var status: Status = Status.NEW_CLIENT,
    var previousStatus: Status = status,
    var weeksPassed: Int = 0,
    var daysPassed: Int = 0,
    var trainingPlan: TrainingPlan = TrainingPlan(0, 0, ""),
    var billId: String = ""
) {

    val trial: Boolean
        get() = weeksPassed < 2

    val trialPeriodEnded: Boolean
        get() = weeksPassed == 2

    val hasCompetition: Boolean
        get() = trainingPlan.activityType == 4
}
