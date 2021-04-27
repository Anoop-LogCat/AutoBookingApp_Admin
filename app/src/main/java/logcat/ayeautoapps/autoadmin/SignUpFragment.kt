package logcat.ayeautoapps.autoadmin

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignUpFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var usernameEditText:TextInputEditText
    private lateinit var phoneEditText:TextInputEditText
    private lateinit var progressDialog:ProgressDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController  = Navigation.findNavController(view)
        val closeButton:ImageView = view.findViewById(R.id.signUpClose_button)
        usernameEditText = view.findViewById(R.id.signUpUsernameEditText)
        phoneEditText = view.findViewById(R.id.signUpPhoneEditText)
        val phoneLayout: TextInputLayout = view.findViewById(R.id.signUpPhoneLayout)
        val userNameLayout: TextInputLayout = view.findViewById(R.id.signUpUserNameLayout)

        closeButton.setOnClickListener {
           navController.navigate(R.id.splashScreeen)
        }

        view.findViewById<RelativeLayout>(R.id.signUpButton).setOnClickListener {
            if(phoneEditText.text.isNullOrBlank()||usernameEditText.text.isNullOrBlank()){
                phoneLayout.helperText="invalid phone number"
                userNameLayout.helperText = "invalid user name"
            }
            else{
                when ((phoneEditText.text.toString()).length) {
                    10 ->{
                        signUpAuth("+91${phoneEditText.text.toString()}")
                    }
                    else -> phoneLayout.helperText="invalid phone number"
                }
            }
        }
    }

    private fun signUpAuth(phoneNumber: String){
        progressDialog= ProgressDialog(requireContext())
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading")
        progressDialog.show()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60,
            TimeUnit.SECONDS, requireActivity(),
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    progressDialog.cancel()
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context,"invalid request",Toast.LENGTH_SHORT).show()
                    } else if (e is FirebaseTooManyRequestsException) {
                        Toast.makeText(context,"time expired",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    progressDialog.cancel()
                    val action =SignUpFragmentDirections.actionSignUpFragmentToVerificationCode()
                    action.verificationCode=verificationId
                    action.username=usernameEditText.text.toString()
                    action.phone=(phoneEditText.text.toString()).toLong()
                    navController.navigate(action)
                }
            })
    }
}