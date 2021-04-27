package logcat.ayeautoapps.autoadmin

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashScreeen : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screeen, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val signUp:Button = view.findViewById(R.id.newAccount)
        val navController:NavController = Navigation.findNavController(view)
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        signUp.visibility=View.INVISIBLE
        Handler().postDelayed({
            if(currentUser!=null){
                navController.navigate(R.id.action_splashScreeen_to_mainMenu)
            }
            else{
                signUp.visibility=View.VISIBLE
                signUp.setOnClickListener {
                    navController.navigate(R.id.action_splashScreeen_to_signUpFragment)
                }
            }
        }, 2000)
    }
}

