package com.ersurajrajput.quizapp.repo

import GameAnswerOption
import GameModel
import GameQuestion
import android.util.Log
import com.ersurajrajput.quizapp.models.AnswerOption
import com.ersurajrajput.quizapp.models.DiagramModel
import com.ersurajrajput.quizapp.models.DictionaryModel
import com.ersurajrajput.quizapp.models.DragAndDropModel
import com.ersurajrajput.quizapp.models.DragAndDropOptions
import com.ersurajrajput.quizapp.models.DragAndDropPages
import com.ersurajrajput.quizapp.models.FillInTheBlanksModel
import com.ersurajrajput.quizapp.models.FillInTheBlanksQuestions
import com.ersurajrajput.quizapp.models.ImageMCQModel
import com.ersurajrajput.quizapp.models.ImageMCQOption
import com.ersurajrajput.quizapp.models.ImageMCQQuestion
import com.ersurajrajput.quizapp.models.MCQActivityModel
import com.ersurajrajput.quizapp.models.MatchImagePair
import com.ersurajrajput.quizapp.models.MatchImagePairPage
import com.ersurajrajput.quizapp.models.MatchPair
import com.ersurajrajput.quizapp.models.MatchPairPage
import com.ersurajrajput.quizapp.models.MatchTextImagePage
import com.ersurajrajput.quizapp.models.MatchTextImagePair
import com.ersurajrajput.quizapp.models.MatchTheFollowingImageAndTextModel
import com.ersurajrajput.quizapp.models.MatchTheFollowingImageModel
import com.ersurajrajput.quizapp.models.MatchTheFollowingModel
import com.ersurajrajput.quizapp.models.Questions
import com.ersurajrajput.quizapp.models.SecretsModel
import com.ersurajrajput.quizapp.models.StaffModel
import com.ersurajrajput.quizapp.models.TrueFalseActivityModel
import com.ersurajrajput.quizapp.models.TrueFalseQuestion
import com.ersurajrajput.quizapp.models.VocabularyWord
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class DummyRepo {
    val db = FirebaseFirestore.getInstance()
    val dummyGame = GameModel(
        id = "g1",
        name = "Ultimate Quiz Challenge",
        description = "Test your knowledge across multiple topics",
        createdBy = "Admin",
        gameType = "SpellBee",
        questions = listOf(
            GameQuestion(
                id = "q1",
                questionText = "What is 2 + 2?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "3", correct = false),
                    GameAnswerOption(id = "o2", text = "4", correct = true),
                    GameAnswerOption(id = "o3", text = "5", correct = false),
                    GameAnswerOption(id = "o4", text = "6", correct = false)
                )
            ),
            GameQuestion(
                id = "q2",
                questionText = "What is the capital of India?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "Mumbai", correct = false),
                    GameAnswerOption(id = "o2", text = "Delhi", correct = true),
                    GameAnswerOption(id = "o3", text = "Kolkata", correct = false),
                    GameAnswerOption(id = "o4", text = "Chennai", correct = false)
                )
            ),
            GameQuestion(
                id = "q3",
                questionText = "Who wrote 'Ramayana'?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "Valmiki", correct = true),
                    GameAnswerOption(id = "o2", text = "Tulsidas", correct = false),
                    GameAnswerOption(id = "o3", text = "Kalidasa", correct = false),
                    GameAnswerOption(id = "o4", text = "Kabir", correct = false)
                )
            ),
            GameQuestion(
                id = "q4",
                questionText = "Which planet is known as the Red Planet?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "Earth", correct = false),
                    GameAnswerOption(id = "o2", text = "Mars", correct = true),
                    GameAnswerOption(id = "o3", text = "Jupiter", correct = false),
                    GameAnswerOption(id = "o4", text = "Venus", correct = false)
                )
            ),
            GameQuestion(
                id = "q5",
                questionText = "What is H2O commonly known as?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "Salt", correct = false),
                    GameAnswerOption(id = "o2", text = "Water", correct = true),
                    GameAnswerOption(id = "o3", text = "Oxygen", correct = false),
                    GameAnswerOption(id = "o4", text = "Hydrogen", correct = false)
                )
            ),
            GameQuestion(
                id = "q6",
                questionText = "Who painted the Mona Lisa?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "Vincent Van Gogh", correct = false),
                    GameAnswerOption(id = "o2", text = "Leonardo da Vinci", correct = true),
                    GameAnswerOption(id = "o3", text = "Pablo Picasso", correct = false),
                    GameAnswerOption(id = "o4", text = "Michelangelo", correct = false)
                )
            ),
            GameQuestion(
                id = "q7",
                questionText = "Which gas do plants produce during photosynthesis?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "Oxygen", correct = true),
                    GameAnswerOption(id = "o2", text = "Carbon Dioxide", correct = false),
                    GameAnswerOption(id = "o3", text = "Nitrogen", correct = false),
                    GameAnswerOption(id = "o4", text = "Hydrogen", correct = false)
                )
            ),
            GameQuestion(
                id = "q8",
                questionText = "Which is the largest ocean in the world?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "Atlantic Ocean", correct = false),
                    GameAnswerOption(id = "o2", text = "Indian Ocean", correct = false),
                    GameAnswerOption(id = "o3", text = "Pacific Ocean", correct = true),
                    GameAnswerOption(id = "o4", text = "Arctic Ocean", correct = false)
                )
            ),
            GameQuestion(
                id = "q9",
                questionText = "What is the boiling point of water at sea level?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "90°C", correct = false),
                    GameAnswerOption(id = "o2", text = "100°C", correct = true),
                    GameAnswerOption(id = "o3", text = "120°C", correct = false),
                    GameAnswerOption(id = "o4", text = "80°C", correct = false)
                )
            ),
            GameQuestion(
                id = "q10",
                questionText = "Which country hosted the 2020 Summer Olympics?",
                options = listOf(
                    GameAnswerOption(id = "o1", text = "China", correct = false),
                    GameAnswerOption(id = "o2", text = "Japan", correct = true),
                    GameAnswerOption(id = "o3", text = "USA", correct = false),
                    GameAnswerOption(id = "o4", text = "Brazil", correct = false)
                )
            )
        )
    )

    val dummyStaffList = listOf(
        StaffModel(
            id = "s1",
            name = "Suraj Rajput",
            email = "suraj@example.com",
            mobile = "7668659783",
            role = "Admin",
            department = "IT",
            password = "admin123"
        ),
        StaffModel(
            id = "s2",
            name = "Anita Sharma",
            email = "anita@example.com",
            mobile = "9876543210",
            role = "Teacher",
            department = "Mathematics",
            password = "teacher123"
        ),
        StaffModel(
            id = "s3",
            name = "Ramesh Singh",
            email = "ramesh@example.com",
            mobile = "9123456780",
            role = "Staff",
            department = "Library",
            password = "staff123"
        ),
        StaffModel(
            id = "s4",
            name = "Priya Verma",
            email = "priya@example.com",
            mobile = "9988776655",
            role = "Teacher",
            department = "Science",
            password = "science123"
        ),
        StaffModel(
            id = "s5",
            name = "Vikram Patel",
            email = "vikram@example.com",
            mobile = "9012345678",
            role = "Admin",
            department = "Finance",
            password = "finance123"
        )
    )

    val dList = listOf(
        DiagramModel(
            id = "d1",
            title = "Parrot",
            desc = "how to draw a parrot",
            url = "https://www.youtube.com/watch?v=fVIB3K8G40g"
        ),
        DiagramModel(
            id = "d2",
            title = "butterfly",
            desc = "how to draw a butterfly",
            url = "https://www.youtube.com/watch?v=mqR3ulVl2RI"
        ),

    )

    val architectureVocabulary = listOf(DictionaryModel(
        id = "1",
        title = "Architecture Vocabulary",
        desc = "Learn common and advanced words related to architecture and construction.",
        vocabularyWord = listOf(
            VocabularyWord(
                word = "Base",
                definition = "The lowest part or edge of something, especially the part on which it rests or is supported.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Base"
            ),
            VocabularyWord(
                word = "Column",
                definition = "A tall vertical structure of stone, wood, or metal, used as a support for a building or as an ornament or monument.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Column"
            ),
            VocabularyWord(
                word = "Capital",
                definition = "The most important city or town of a country or region, usually its seat of government.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Capital"
            ),
            VocabularyWord(
                word = "Foundation",
                definition = "The lowest load-bearing part of a building, typically below ground level.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Foundation"
            ),
            VocabularyWord(
                word = "Structure",
                definition = "The arrangement of and relations between the parts or elements of something complex.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Structure"
            ),
            VocabularyWord(
                word = "Zenith",
                definition = "The time at which something is most powerful or successful.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Zenith"
            ),
            VocabularyWord(
                word = "Arch",
                definition = "A curved structure forming the upper edge of a doorway, window, or bridge.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Arch"
            ),
            VocabularyWord(
                word = "Facade",
                definition = "The front of a building, especially an imposing or decorative one.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Facade"
            ),
            VocabularyWord(
                word = "Lintel",
                definition = "A horizontal support across the top of a door or window.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Lintel"
            ),
            VocabularyWord(
                word = "Masonry",
                definition = "Stonework or brickwork; building with individual units bound together by mortar.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Masonry"
            ),
            VocabularyWord(
                word = "Pediment",
                definition = "A triangular upper part of the front of a building in classical style.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Pediment"
            ),
            VocabularyWord(
                word = "Truss",
                definition = "A framework, typically consisting of rafters, posts, and struts, supporting a roof or bridge.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Truss"
            ),
            VocabularyWord(
                word = "Buttress",
                definition = "A projecting support built against a wall to strengthen it.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Buttress"
            ),
            VocabularyWord(
                word = "Cornice",
                definition = "An ornamental molding around the wall of a room just below the ceiling or at the top of a building.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Cornice"
            ),
            VocabularyWord(
                word = "Dome",
                definition = "A rounded vault forming the roof of a building or structure, typically with a circular base.",
                imageUrl = "https://placehold.co/600x400/E1CBA8/5D4037?text=Dome"
            )
        )
    )
    )
    val sampleMcqActivity = MCQActivityModel(
        id = "activity_001",
        title = "General Knowledge Quiz",
        desc = "A short quiz to test your basic GK skills.",
        qList = listOf(
            Questions(
                id = "q1",
                qTitle = "Who is known as the father of computers?",
                answerOption = listOf(
                    AnswerOption(id = "1", text = "Charles Babbage", correct = true),
                    AnswerOption(id = "2", text = "Isaac Newton", correct = false),
                    AnswerOption(id = "3", text = "Alan Turing", correct = false),
                    AnswerOption(id = "4", text = "Albert Einstein", correct = false)
                )
            ),
            Questions(
                id = "q2",
                qTitle = "Which planet is known as the Red Planet?",
                answerOption = listOf(
                    AnswerOption(id = "1", text = "Earth", correct = false),
                    AnswerOption(id = "2", text = "Mars", correct = true),
                    AnswerOption(id = "3", text = "Jupiter", correct = false),
                    AnswerOption(id = "4", text = "Venus", correct = false)
                )
            ),
            Questions(
                id = "q3",
                qTitle = "What is the capital of France?",
                answerOption = listOf(
                    AnswerOption(id = "1", text = "Berlin", correct = false),
                    AnswerOption(id = "2", text = "Madrid", correct = false),
                    AnswerOption(id = "3", text = "Paris", correct = true),
                    AnswerOption(id = "4", text = "Lisbon", correct = false)
                )
            )
        )
    )

    val sampleTrueFalseActivity = TrueFalseActivityModel(
        id = "activity_001",
        title = "General Knowledge Quiz",
        desc = "Test your general knowledge with these true/false questions!",
        questions = listOf(
            TrueFalseQuestion(
                id = "q1",
                text = "The Earth revolves around the Sun.",
                correctAnswer = true
            ),
            TrueFalseQuestion(
                id = "q2",
                text = "The capital of Australia is Sydney.",
                correctAnswer = false
            ),
            TrueFalseQuestion(
                id = "q3",
                text = "Water freezes at 0 degrees Celsius.",
                correctAnswer = true
            ),
            TrueFalseQuestion(
                id = "q4",
                text = "The human body has four lungs.",
                correctAnswer = false
            ),
            TrueFalseQuestion(
                id = "q5",
                text = "Light travels faster than sound.",
                correctAnswer = true
            ),
            TrueFalseQuestion(
                id = "q6",
                text = "Mount Everest is the tallest mountain in the world.",
                correctAnswer = true
            ),
            TrueFalseQuestion(
                id = "q7",
                text = "Sharks are mammals.",
                correctAnswer = false
            ),
            TrueFalseQuestion(
                id = "q8",
                text = "The Great Wall of China is visible from the Moon.",
                correctAnswer = false
            ),
            TrueFalseQuestion(
                id = "q9",
                text = "Venus is the closest planet to the Sun.",
                correctAnswer = false
            ),
            TrueFalseQuestion(
                id = "q10",
                text = "Bananas grow on trees.",
                correctAnswer = false
            )
        )
    )

        fun getAllDummyStaffFromFirebase(): Task<QuerySnapshot?> {
            val staffCollection = db.collection("staffs")

            return staffCollection.get()
        }
        fun getAllDummyStaff(): List<StaffModel> {
            return dummyStaffList
        }
        fun getDummyStaffById(id: String): StaffModel {
            var staff = dummyStaffList.filter { it.id == id }
            if (staff.isEmpty()) {
                var emptyStaff = StaffModel()
                Log.d("DummyRepo", "No staff found with id: $id, returning first staff as fallback")
                return emptyStaff
            }
            return staff[0]
        }


        fun populateStaff() {
            var staffCollection = db.collection("staffs")
            dummyStaffList.forEach { staff ->
                staffCollection.add(staff)
            }
        }
        fun getGame(): GameModel {
            return dummyGame
        }
        fun dummySaveGame(game: GameModel): Boolean {
            return true
        }
        fun populateDummyGames() {
            val gamesCollection = db.collection("games")
            gamesCollection.add(dummyGame)
        }
        fun getAllDummyDataFromFirebse(): Task<QuerySnapshot?> {
            val gamesCollection = db.collection("games")

            return gamesCollection.get()
        }

        fun getAllDiagrams(): List<DiagramModel> {
            return dList;
        }
        fun saveDiagramToFireBase() {
            val diagramCollection = db.collection("diagrams")
            dList.forEach {
                diagramCollection.add(it)
            }

        }
        fun getDictionary(): DictionaryModel {
            return architectureVocabulary[0]
        }
        fun getDictionaryList(): List<DictionaryModel> {
            return architectureVocabulary
        }
        fun saveDictonaryToFireBase() {
            val dict = db.collection("dictionary")
            dict.add(architectureVocabulary[0])

        }
        fun populateMcqActivity() {
            var collections = db.collection("mcqActivity")
            collections.add(sampleMcqActivity)
        }

    fun populateTrueFalseActivivty(){
        var colletion = db.collection("truefalse")
        colletion.add(sampleTrueFalseActivity)
    }


    val dummyImageMCQActivity = ImageMCQModel(
        id = "activity1",
        title = "Image-Based Quiz",
        desc = "Select the correct image for each question",
        questions = listOf(
            ImageMCQQuestion(
                id = "q1",
                text = "Which image shows the Eiffel Tower?",
                options = listOf(
                    ImageMCQOption(id = "1", imageUrl = "https://example.com/eiffel1.jpg"),
                    ImageMCQOption(id = "2", imageUrl = "https://example.com/eiffel2.jpg"),
                    ImageMCQOption(id = "3", imageUrl = "https://example.com/eiffel3.jpg"),
                    ImageMCQOption(id = "4", imageUrl = "https://example.com/eiffel4.jpg")
                ),
                correctOptionIndex = 2
            ),
            ImageMCQQuestion(
                id = "q2",
                text = "Which image shows the Statue of Liberty?",
                options = listOf(
                    ImageMCQOption(id = "1", imageUrl = "https://example.com/liberty1.jpg"),
                    ImageMCQOption(id = "2", imageUrl = "https://example.com/liberty2.jpg"),
                    ImageMCQOption(id = "3", imageUrl = "https://example.com/liberty3.jpg"),
                    ImageMCQOption(id = "4", imageUrl = "https://example.com/liberty4.jpg")
                ),
                correctOptionIndex = 0
            ),
            ImageMCQQuestion(
                id = "q3",
                text = "Which image shows the Great Wall of China?",
                options = listOf(
                    ImageMCQOption(id = "1", imageUrl = "https://example.com/greatwall1.jpg"),
                    ImageMCQOption(id = "2", imageUrl = "https://example.com/greatwall2.jpg"),
                    ImageMCQOption(id = "3", imageUrl = "https://example.com/greatwall3.jpg"),
                    ImageMCQOption(id = "4", imageUrl = "https://example.com/greatwall4.jpg")
                ),
                correctOptionIndex = 1
            ),
            ImageMCQQuestion(
                id = "q4",
                text = "Which image shows the Taj Mahal?",
                options = listOf(
                    ImageMCQOption(id = "1", imageUrl = "https://example.com/taj1.jpg"),
                    ImageMCQOption(id = "2", imageUrl = "https://example.com/taj2.jpg"),
                    ImageMCQOption(id = "3", imageUrl = "https://example.com/taj3.jpg"),
                    ImageMCQOption(id = "4", imageUrl = "https://example.com/taj4.jpg")
                ),
                correctOptionIndex = 3
            )
        )
    )
    fun populateImageMCQ(){
        var c = db.collection("imageMcq")
        c.add(dummyImageMCQActivity)
    }
    val generalKnowledgeQuiz = FillInTheBlanksModel(
        id = "FIB_GK_001",
        title = "General Knowledge Challenge",
        desc = "Test your general knowledge by filling in the missing word in each sentence.",
        questions = listOf(
            FillInTheBlanksQuestions(
                id = "GKQ1",
                text = "The planet known as the Red Planet is _.",
                ans = "Mars"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ2",
                text = "The largest ocean on Earth is the _ Ocean.",
                ans = "Pacific"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ3",
                text = "The capital of Japan is _.",
                ans = "Tokyo"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ4",
                text = "Sound travels fastest through _.",
                ans = "solids"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ5",
                text = "The Great Wall of China was built to protect against invasions from the _.",
                ans = "north"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ6",
                text = "The chemical symbol for gold is _.",
                ans = "Au"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ7",
                text = "The study of earthquakes is called _.",
                ans = "seismology"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ8",
                text = "The 'Mona Lisa' was painted by Leonardo da _.",
                ans = "Vinci"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ9",
                text = "The process by which plants make their food is known as _.",
                ans = "photosynthesis"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ10",
                text = "The first person to walk on the moon was Neil _.",
                ans = "Armstrong"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ11",
                text = "The currency of the United Kingdom is the Pound _.",
                ans = "Sterling"
            ),
            FillInTheBlanksQuestions(
                id = "GKQ12",
                text = "The highest mountain in the world is Mount _.",
                ans = "Everest"
            )
        )
    )
    fun populateFillInTheBlanks(){
        var c = db.collection("fillInTheBlanks")
        c.add(generalKnowledgeQuiz)
    }


    val dummyMatchQuiz = MatchTheFollowingModel(
        id = "quiz_001",
        title = "Match the Following Quiz",
        desc = "Connect the items on the left with their correct matches on the right.",
        pages = listOf(
            // Page 1
            MatchPairPage(
                id = "page_001",
                pairs = listOf(
                    MatchPair(id = "1", leftOption = "Apple", rightOption = "Fruit"),
                    MatchPair(id = "2", leftOption = "Carrot", rightOption = "Vegetable"),
                    MatchPair(id = "3", leftOption = "Rose", rightOption = "Flower"),
                    MatchPair(id = "4", leftOption = "Dog", rightOption = "Animal"),
                    MatchPair(id = "5", leftOption = "Gold", rightOption = "Metal")
                )
            ),

            // Page 2
            MatchPairPage(
                id = "page_002",
                pairs = listOf(
                    MatchPair(id = "6", leftOption = "Sun", rightOption = "Star"),
                    MatchPair(id = "7", leftOption = "Earth", rightOption = "Planet"),
                    MatchPair(id = "8", leftOption = "Moon", rightOption = "Satellite")
                )
            ),

            // Page 3
            MatchPairPage(
                id = "page_003",
                pairs = listOf(
                    MatchPair(id = "9", leftOption = "Oxygen", rightOption = "Gas"),
                    MatchPair(id = "10", leftOption = "Water", rightOption = "Liquid"),
                    MatchPair(id = "11", leftOption = "Iron", rightOption = "Metal"),
                    MatchPair(id = "12", leftOption = "Diamond", rightOption = "Gemstone")
                )
            )
        )
    )
    fun populateMatchTheFollowing(){
        var c = db.collection("matchTheFollowing")
        c.add(dummyMatchQuiz)
    }

    val dummyImageQuizList =
        MatchTheFollowingImageModel(
            id = "quiz1",
            title = "Match the Animals",
            desc = "Match each animal with its habitat.",
            pages = listOf(
                MatchImagePairPage(
                    id = "page1",
                    pairs = listOf(
                        MatchImagePair(
                            id = "pair1",
                            leftImageUrl = "https://example.com/images/lion.png",
                            rightImageUrl = "https://example.com/images/savannah.png"
                        ),
                        MatchImagePair(
                            id = "pair2",
                            leftImageUrl = "https://example.com/images/penguin.png",
                            rightImageUrl = "https://example.com/images/antarctica.png"
                        ),
                        MatchImagePair(
                            id = "pair3",
                            leftImageUrl = "https://example.com/images/dolphin.png",
                            rightImageUrl = "https://example.com/images/ocean.png"
                        )
                    )
                ),
                MatchImagePairPage(
                    id = "page2",
                    pairs = listOf(
                        MatchImagePair(
                            id = "pair4",
                            leftImageUrl = "https://example.com/images/elephant.png",
                            rightImageUrl = "https://example.com/images/jungle.png"
                        ),
                        MatchImagePair(
                            id = "pair5",
                            leftImageUrl = "https://example.com/images/eagle.png",
                            rightImageUrl = "https://example.com/images/mountains.png"
                        )
                    )
                )
            )
        )




    fun populateMatchTheFollowingImage(){
        var c = db.collection("matchTheFollowingImage")
        c.add(dummyImageQuizList)
    }


    val matchTheFollowingTextImageList =
        MatchTheFollowingImageAndTextModel(
            id = "quiz001",
            title = "Match Fruits with Images",
            desc = "Match each fruit name with its correct image",
            pages = listOf(
                MatchTextImagePage(
                    id = "page1",
                    pairs = listOf(
                        MatchTextImagePair(
                            id = "pair1",
                            leftText = "Apple",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/1/15/Red_Apple.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair2",
                            leftText = "Banana",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/8/8a/Banana-Single.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair3",
                            leftText = "Grapes",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/1/16/Table_grapes_on_white.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair4",
                            leftText = "Mango",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/90/Hapus_Mango.jpg"
                        ),
                    )
                ),
                MatchTextImagePage(
                    id = "page2",
                    pairs = listOf(
                        MatchTextImagePair(
                            id = "pair5",
                            leftText = "Orange",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c4/Orange-Fruit-Pieces.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair6",
                            leftText = "Strawberry",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/2/29/PerfectStrawberry.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair7",
                            leftText = "Pineapple",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/c/cb/Pineapple_and_cross_section.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair8",
                            leftText = "Watermelon",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/f/fd/Watermelon_cross_BNC.jpg"
                        ),
                    )
                ),
                MatchTextImagePage(
                    id = "page3",
                    pairs = listOf(
                        MatchTextImagePair(
                            id = "pair9",
                            leftText = "Kiwi",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d3/Kiwi_aka.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair10",
                            leftText = "Papaya",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/1/1f/Papaya_cross_section_BNC.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair11",
                            leftText = "Cherry",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/b/bb/Cherry_Stella444.jpg"
                        ),
                        MatchTextImagePair(
                            id = "pair12",
                            leftText = "Guava",
                            rightImageUrl = "https://upload.wikimedia.org/wikipedia/commons/f/f4/Guava_ID.jpg"
                        ),
                    )
                )
            )
        )

    fun populateMatchTheFollowingTextAndImage(){
        var c = db.collection("matchTheFollowingTextAndImage")
        c.add(matchTheFollowingTextImageList)
    }

    val dragAndDropData = DragAndDropModel(
        id = "drag1",
        title = "Fruits and Animals Drag & Drop",
        desc = "Drag and drop the items to their correct categories or positions.",
        pages = listOf(
            DragAndDropPages(
                id = "page1",
                options = listOf(
                    DragAndDropOptions(
                        id = "opt1",
                        name = "Apple",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/1/15/Red_Apple.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt2",
                        name = "Banana",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/8/8a/Banana-Single.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt3",
                        name = "Orange",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/c/c4/Orange-Fruit-Pieces.jpg"
                    )
                )
            ),
            DragAndDropPages(
                id = "page2",
                options = listOf(
                    DragAndDropOptions(
                        id = "opt4",
                        name = "Mango",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/9/90/Hapus_Mango.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt5",
                        name = "Grapes",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/b/bb/Table_grapes_on_white.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt6",
                        name = "Watermelon",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/f/fd/Watermelon_cross_BNC.jpg"
                    )
                )
            ),
            DragAndDropPages(
                id = "page3",
                options = listOf(
                    DragAndDropOptions(
                        id = "opt7",
                        name = "Dog",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/5/5f/Dog_puppy.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt8",
                        name = "Cat",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/3/3a/Cat03.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt9",
                        name = "Elephant",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/3/37/African_Bush_Elephant.jpg"
                    )
                )
            ),
            DragAndDropPages(
                id = "page4",
                options = listOf(
                    DragAndDropOptions(
                        id = "opt10",
                        name = "Lion",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/7/73/Lion_waiting_in_Namibia.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt11",
                        name = "Tiger",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/5/56/Tiger.50.jpg"
                    ),
                    DragAndDropOptions(
                        id = "opt12",
                        name = "Rabbit",
                        imageUri = "https://upload.wikimedia.org/wikipedia/commons/8/88/European_rabbit_in_grass_field.jpg"
                    )
                )
            )
        )
    )

    fun populateDragAndDrop(){
        var c = db.collection("dragAndDrop")
        c.add(dragAndDropData)
    }



}