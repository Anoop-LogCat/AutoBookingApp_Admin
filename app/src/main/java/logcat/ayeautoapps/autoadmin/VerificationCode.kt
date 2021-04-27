package logcat.ayeautoapps.autoadmin

import `in`.aabhasjindal.otptextview.OtpTextView
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging

class VerificationCode : Fragment() {

    private lateinit var navController: NavController
    private val argInVerificationCode:VerificationCodeArgs by navArgs()
    private val auth: FirebaseAuth =FirebaseAuth.getInstance()
    private lateinit var progressDialogInVerification:ProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_verification_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController  = Navigation.findNavController(view)
        val verifyEdiText:OtpTextView=view.findViewById(R.id.otpEditTextForVerify)
        view.findViewById<Button>(R.id.verify_button).setOnClickListener {
            if(verifyEdiText.otp?.length==6) {
                progressDialogInVerification= ProgressDialog(requireContext())
                progressDialogInVerification.setMessage("Verifying")
                progressDialogInVerification.setCancelable(false)
                progressDialogInVerification.show()
                val credential = PhoneAuthProvider.getCredential(argInVerificationCode.verificationCode, verifyEdiText.otp!!)
                auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()){ task->
                    if (task.isSuccessful) {
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task2->
                                if(task2.isSuccessful){
                                    val token:String=task2.result
                                    val user = mapOf("username" to argInVerificationCode.username, "token" to token,"phone" to argInVerificationCode.phone)
                                    FirebaseFunctions.getInstance().getHttpsCallable("SaveAdmin").call(user).addOnCompleteListener{
                                        if(it.isSuccessful){
                                            progressDialogInVerification.cancel()
                                             navController.navigate(R.id.action_verificationCode_to_mainMenu)
                                        }else{
                                            Toast.makeText(requireContext(),"unable to upload your data",Toast.LENGTH_SHORT).show()
                                            progressDialogInVerification.cancel()
                                        }
                                    }
                                }else{
                                    Toast.makeText(requireContext(),"token fetch failed",Toast.LENGTH_SHORT).show()
                                    progressDialogInVerification.cancel()
                                }
                            }
                    }
                    else {
                        progressDialogInVerification.cancel()
                        if (task.exception is FirebaseAuthInvalidCredentialsException) { Toast.makeText(context, "Invalid Code", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
            else{
                Toast.makeText(context, "code invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}