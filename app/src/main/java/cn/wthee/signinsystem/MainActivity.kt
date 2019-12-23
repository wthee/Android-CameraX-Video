package cn.wthee.signinsystem

import android.Manifest.permission
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.wthee.signinsystem.ui.main.MainFragment
import com.tbruyelle.rxpermissions2.RxPermissions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .request(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE, permission.RECORD_AUDIO)
            .subscribe { granted ->
                if (granted) {

                } else {

                }
            }
    }

}
