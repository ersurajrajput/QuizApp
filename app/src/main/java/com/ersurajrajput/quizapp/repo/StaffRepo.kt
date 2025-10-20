package com.ersurajrajput.quizapp.repo

import android.util.Log
import com.ersurajrajput.quizapp.models.StaffModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject

class StaffRepo {
    private val db = FirebaseFirestore.getInstance()
     val staffCollection = db.collection("staffs")

    fun getStaffList(onResult: (MutableList<StaffModel>) -> Unit) {
        staffCollection.get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("myApp", "Documents fetched: ${querySnapshot.size()}")
                val staffList = querySnapshot.documents.mapNotNull { doc ->
                    Log.d("myApp", "Document ID: ${doc.id}, Data: ${doc.data}")
                    doc.toObject(StaffModel::class.java)?.apply { id = doc.id }
                }.toMutableList()
                Log.d("myApp", "Mapped Staff List: $staffList")
                onResult(staffList)
            }
            .addOnFailureListener {
                e -> Log.e("myApp", "Error fetching staff list", e)
            }

    }
    fun deleteStaff(staffId: String, onComplete: (Boolean) -> Unit) {
        staffCollection.document(staffId).delete()
            .addOnSuccessListener {
                Log.d("myApp", "Staff with ID $staffId deleted successfully")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("myApp", "Error deleting staff with ID $staffId", e)
                onComplete(false)
            }
    }

    fun addStaff(staff: StaffModel, onComplete: (Boolean, String?) -> Unit) {
        // If staff has no id, generate a new one
        val docRef = if (staff.id.isBlank()) {
            staffCollection.document()
        } else {
            staffCollection.document(staff.id)
        }

        staff.id = docRef.id

        docRef.set(staff)
            .addOnSuccessListener {
                Log.d("myApp", "Staff added/updated with ID: ${staff.id}")
                onComplete(true, staff.id)
            }
            .addOnFailureListener { e ->
                Log.e("myApp", "Error adding/updating staff", e)
                onComplete(false, null)
            }
    }
    fun updateStaff(id: String, staff: StaffModel, onComplete: (Boolean) -> Unit) {
        if (staff.id.isBlank()) {
            Log.e("myApp", "Staff ID is blank, cannot edit")
            onComplete(false)
            return
        }

        staffCollection.document(staff.id).set(staff)
            .addOnSuccessListener {
                Log.d("myApp", "Staff with ID ${staff.id} updated successfully")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("myApp", "Error updating staff with ID ${staff.id}", e)
                onComplete(false)
            }
    }



    fun findStaffByIdAndPass(
        email: String,
        pass: String,
        onSuccess: (StaffModel) -> Unit,
        onResult: (Boolean, String) -> Unit
    ) {
        Log.d("Firestore", "Searching for: email=${email.trim()} password=${pass.trim()}")

        db.collection("staffs")
            .whereEqualTo("email", email.trim())
            .whereEqualTo("password", pass.trim())
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("Firestore", "Documents found: ${querySnapshot.size()}")
                for (doc in querySnapshot.documents) {
                    Log.d("Firestore", "Document data: ${doc.data}")
                }

                if (!querySnapshot.isEmpty) {
                    val staff = querySnapshot.documents[0].toObject(StaffModel::class.java)
                    if (staff != null) {
                        onSuccess(staff) // ✅ Pass staff object if found
                        onResult(true, "Login successful") // ✅ Notify success
                    } else {
                        onResult(false, "Staff data invalid")
                    }
                } else {
                    onResult(false, "No staff found with provided credentials")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error querying staff", exception)
                onResult(false, "Error: ${exception.message}")
            }
    }




}
