package com.timesync.app

import android.content.Intent
import com.timesync.app.R
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RoleSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_select)

        findViewById<Button>(R.id.btnMaster).setOnClickListener {
            startActivity(Intent(this, MasterActivity::class.java))
        }
        findViewById<Button>(R.id.btnSlave).setOnClickListener {
            startActivity(Intent(this, SlaveActivity::class.java))
        }
    }
}
