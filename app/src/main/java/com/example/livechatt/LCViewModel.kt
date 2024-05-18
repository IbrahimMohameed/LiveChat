package com.example.livechatt

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.livechatt.data.CHATS
import com.example.livechatt.data.ChatData
import com.example.livechatt.data.ChatUser
import com.example.livechatt.data.Event
import com.example.livechatt.data.MESSAGE
import com.example.livechatt.data.Message
import com.example.livechatt.data.STATUS
import com.example.livechatt.data.Status
import com.example.livechatt.data.USER_MODE
import com.example.livechatt.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var dp: FirebaseFirestore,
    var storage: FirebaseStorage
) : ViewModel() {

    var inProgress = mutableStateOf(false)
    var inProgressChats = mutableStateOf(false)
    var eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    var userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null
    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = dp.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timestamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun dePopulateMessages() {
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }


    fun populateChats() {
        inProgressChats.value = true
        dp.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProgressChats.value = false
            }
        }

    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        dp.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)
    }

    fun signUp(name: String, number: String, email: String, password: String) {
        inProgress.value = true
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill All fields")
            return
        }
        inProgress.value = true
        dp.collection(USER_MODE).whereEqualTo("number", number).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(name, number)
                    } else {
                        handleException(exception = it.exception, customMessage = "Sign Up Failed")
                    }
                }
            } else {
                handleException(customMessage = "Number Already Exists")
                inProgress.value = false
            }
        }

    }

    fun signIn(email: String, password: String) {
        inProgress.value = true
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill All fields")
            return
        } else {
            inProgress.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(exception = it.exception, customMessage = "Sign In Failed")
                }
            }

        }


    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMessage else customMessage
        eventMutableState.value = Event(message)
        inProgress.value = false
    }

    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null
    ) {
        val uId = auth.currentUser?.uid
        uId?.let { userId ->
            val userDataRef = dp.collection(USER_MODE).document(userId)
            userDataRef.get().addOnSuccessListener { userDataSnapshot ->
                val existingUserData = userDataSnapshot.toObject<UserData>()
                val updatedName = name ?: existingUserData?.name
                val updatedNumber = number ?: existingUserData?.number
                val updatedImageUrl = imageUrl ?: existingUserData?.imageUrl

                val updatedUserData = UserData(
                    userId = userId,
                    name = updatedName,
                    number = updatedNumber,
                    imageUrl = updatedImageUrl
                )

                userDataRef.set(updatedUserData)
                    .addOnSuccessListener {
                        inProgress.value = false
                        getUserData(userId) // Optionally, update local user data after setting Firestore document
                    }
                    .addOnFailureListener { exception ->
                        handleException(exception, "Failed to update profile")
                    }
            }.addOnFailureListener { exception ->
                handleException(exception, "Failed to retrieve user data")
            }
        }
    }


    private fun getUserData(uid: String) {
        inProgress.value = true
        dp.collection(USER_MODE).document(uid).addSnapshotListener { value, error ->
            if (value != null) {
                var user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                populateChats()
                populateStatutes()
            }
            if (error != null) {
                handleException(error, "Cannot Retrieve User")
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProgress.value = false
        }.addOnFailureListener {
            handleException(exception = it)
        }
    }

    fun logOut() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        dePopulateMessages()
        currentChatMessageListener = null
        eventMutableState.value = Event("Logged Out")
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly()) {
            handleException(customMessage = "Number must contains digits only")
        } else {
            dp.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ),
                    Filter.and(
                        Filter.equalTo("user1.number", userData.value?.number),
                        Filter.equalTo("user2.number", number)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    dp.collection(USER_MODE).whereEqualTo("number", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(customMessage = "Number Not Found")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = dp.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.imageUrl,
                                        userData.value?.number
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.imageUrl,
                                        chatPartner.number
                                    )
                                )
                                dp.collection(CHATS).document(id).set(chat)
                            }
                        }.addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chat Already Exist")
                }
            }
        }
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri) {
            createStatus(it.toString())
        }
    }

    fun createStatus(imageUrl: String?) {
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.name,
                userData.value?.imageUrl,
                userData.value?.number
            ),
            imageUrl,
            System.currentTimeMillis()
        )
        dp.collection(STATUS).document().set(newStatus)
    }

    fun populateStatutes() {
        val timeDelta = 24L * 60 * 60 * 1000
        val cutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        dp.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                val currentConnections = arrayListOf(userData.value?.userId)
                val chats = value.toObjects<ChatData>()
                chats.forEach { chat ->
                    if (chat.user2.userId == userData.value?.userId) {
                        currentConnections.add(chat.user2.userId)
                    } else {
                        currentConnections.add(chat.user1.userId)
                    }
                }
                dp.collection(STATUS).whereGreaterThan("timestamp", cutOff)
                    .whereIn("user.userId", currentConnections)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            handleException(error)
                        }
                        if (value != null) {
                            status.value = value.toObjects()
                            inProgressStatus.value = false
                        }
                    }
            }
        }
    }
}