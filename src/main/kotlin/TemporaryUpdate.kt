import api.vk.VKApiClient
import keyboards.MainKeyboardAfterPayment
import keyboards.MainKeyboardBeforePayment

suspend fun temporaryUpdate(clientsRepository: ClientsRepository, vkApiClient: VKApiClient) {
    listOf(
        Client(id=192085, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=6, week=0), interviewResults=mutableListOf(), billId=""),
                Client(id=5946748, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=6, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=15733972, trial=true, status=Status.WAITING_FOR_PAYMENT, previousStatus=Status.WAITING_FOR_RESULTS, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=10, week=1), interviewResults=mutableListOf(), billId="FUqmsnhF0L"),
    Client(id=22292537, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=9, hours=10, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=23661247, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=16, weeksPassed=3, trainingPlan=TrainingPlan(month=9, hours=10, week=3), interviewResults=mutableListOf(), billId="cDusKMDLYs"),
    Client(id=42621053, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=6, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=59763375, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=9, hours=10, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=62475899, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=10, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=81087718, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=22, weeksPassed=3, trainingPlan=TrainingPlan(month=9, hours=6, week=3), interviewResults=mutableListOf(), billId="38V6EB95z5"),
    Client(id=95107193, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=9, hours=10, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=99129234, trial=true, status=Status.NEW_CLIENT, previousStatus=Status.NEW_CLIENT, daysPassed=-1, weeksPassed=-1, trainingPlan=TrainingPlan(month=-1, hours=-1, week=-1), interviewResults=mutableListOf(), billId=""),
    Client(id=137823763, trial=true, status=Status.NEW_CLIENT, previousStatus=Status.NEW_CLIENT, daysPassed=-1, weeksPassed=-1, trainingPlan=TrainingPlan(month=-1, hours=-1, week=-1), interviewResults=mutableListOf(), billId=""),
    Client(id=139629980, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=10, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=143964633, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=22, weeksPassed=4, trainingPlan=TrainingPlan(month=8, hours=10, week=4), interviewResults=mutableListOf(), billId="g6c9utAoFk"),
    Client(id=151329887, trial=true, status=Status.WAITING_FOR_PAYMENT, previousStatus=Status.WAITING_FOR_RESULTS, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=10, week=1), interviewResults=mutableListOf(), billId="Tg6RvwmnZ7"),
    Client(id=166138003, trial=true, status=Status.WAITING_FOR_PAYMENT, previousStatus=Status.WAITING_FOR_RESULTS, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=6, week=1), interviewResults=mutableListOf(), billId="Dt4VcLao4o"),
    Client(id=217619042, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=1, weeksPassed=1, trainingPlan=TrainingPlan(month=9, hours=6, week=1), interviewResults=mutableListOf(), billId="HChWKsSVWV"),
    Client(id=247100783, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=2, weeksPassed=1, trainingPlan=TrainingPlan(month=9, hours=10, week=1), interviewResults=mutableListOf(), billId="fDwaKq_UA7"),
    Client(id=255408264, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=19, weeksPassed=4, trainingPlan=TrainingPlan(month=9, hours=6, week=4), interviewResults=mutableListOf(), billId="ImRnpbqK74"),
    Client(id=276633624, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=10, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=298600504, trial=true, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=6, week=0), interviewResults=mutableListOf(), billId=""),
    Client(id=354341060, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=20, weeksPassed=4, trainingPlan=TrainingPlan(month=8, hours=10, week=4), interviewResults=mutableListOf(), billId="gCeOWFQIO6"),
    Client(id=442508718, trial=true, status=Status.WAITING_FOR_PAYMENT, previousStatus=Status.WAITING_FOR_RESULTS, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=10, week=1), interviewResults=mutableListOf(), billId="45eRMPg_wv"),
    Client(id=464281827, trial=false, status=Status.ACTIVE, previousStatus=Status.WAITING_FOR_START, daysPassed=19, weeksPassed=3, trainingPlan=TrainingPlan(month=9, hours=10, week=3), interviewResults=mutableListOf(), billId="cxQVRcvzga"),
    Client(id=569106420, trial=true, status=Status.WAITING_FOR_RESULTS, previousStatus=Status.ACTIVE, daysPassed=-1, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=6, week=0), interviewResults=mutableListOf(1), billId=""),
    Client(id=665090251, trial=false, status=Status.WAITING_FOR_PAYMENT, previousStatus=Status.WAITING_FOR_START, daysPassed=28, weeksPassed=0, trainingPlan=TrainingPlan(month=8, hours=10, week=1), interviewResults=mutableListOf(), billId="cvTj2Ysitm")
    ).forEach { clientsRepository.add(it) }
}