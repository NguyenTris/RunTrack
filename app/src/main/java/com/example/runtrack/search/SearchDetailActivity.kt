package com.example.runtrack.search

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.runtrack.R
import com.example.runtrack.chat.ChatDetailActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class SearchDetailActivity : AppCompatActivity() {

    private lateinit var tvTopTitle: TextView
    private lateinit var ivAvatar: ShapeableImageView
    private lateinit var ivGroupIcon: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvSubtitle: TextView

    private lateinit var layoutFriendSection: LinearLayout
    private lateinit var layoutClubSection: LinearLayout

    private lateinit var btnFriendAdd: MaterialButton
    private lateinit var btnFriendChat: MaterialButton
    private lateinit var btnClubJoin: MaterialButton

    private var isFriendAdded = false
    private var isClubJoined = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_detail)

        val root = findViewById<View>(R.id.root_search_detail)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                it.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val name = intent.getStringExtra("EXTRA_ITEM_NAME") ?: "Đức Huy"
        val subtitle = intent.getStringExtra("EXTRA_ITEM_SUBTITLE") ?: ""
        val typeName = intent.getStringExtra("EXTRA_ITEM_TYPE") ?: SearchType.FRIEND.name
        val type = try { SearchType.valueOf(typeName) } catch (e: Exception) { SearchType.FRIEND }

        initViews()
        setupListeners(name, subtitle, type)
        bindData(name, subtitle, type)
    }

    private fun initViews() {
        tvTopTitle = findViewById(R.id.tv_detail_top_title)
        ivAvatar = findViewById(R.id.iv_profile_avatar)
        ivGroupIcon = findViewById(R.id.iv_profile_group_icon)
        tvName = findViewById(R.id.tv_profile_name)
        tvSubtitle = findViewById(R.id.tv_profile_subtitle)

        layoutFriendSection = findViewById(R.id.layout_friend_section)
        layoutClubSection = findViewById(R.id.layout_club_section)

        btnFriendAdd = findViewById(R.id.btn_friend_add)
        btnFriendChat = findViewById(R.id.btn_friend_chat)
        btnClubJoin = findViewById(R.id.btn_club_join)
    }

    private fun setupListeners(name: String, subtitle: String, type: SearchType) {
        findViewById<ImageView>(R.id.btn_detail_back)?.setOnClickListener {
            finish()
        }

        btnFriendAdd.setOnClickListener {
            isFriendAdded = !isFriendAdded
            if (isFriendAdded) {
                btnFriendAdd.text = "Đã gửi lời mời"
                btnFriendAdd.setBackgroundColor(Color.parseColor("#22262B"))
                btnFriendAdd.setTextColor(Color.parseColor("#8E8E93"))
                Toast.makeText(this, "Đã gửi lời mời kết bạn đến $name", Toast.LENGTH_SHORT).show()
            } else {
                btnFriendAdd.text = "Kết bạn"
                btnFriendAdd.setBackgroundColor(Color.parseColor("#F4C073"))
                btnFriendAdd.setTextColor(Color.parseColor("#121212"))
                Toast.makeText(this, "Đã hủy lời mời kết bạn", Toast.LENGTH_SHORT).show()
            }
        }

        btnFriendChat.setOnClickListener {
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("EXTRA_CHAT_NAME", name)
                putExtra("EXTRA_LAST_MSG", subtitle)
                putExtra("EXTRA_IS_GROUP", type == SearchType.CLUB)
                putExtra("EXTRA_IS_ONLINE", true)
            }
            startActivity(intent)
        }

        btnClubJoin.setOnClickListener {
            isClubJoined = !isClubJoined
            if (isClubJoined) {
                btnClubJoin.text = "Đã tham gia CLB"
                btnClubJoin.setBackgroundColor(Color.parseColor("#22262B"))
                btnClubJoin.setTextColor(Color.parseColor("#8E8E93"))
                Toast.makeText(this, "Chào mừng bạn đến với $name!", Toast.LENGTH_SHORT).show()
            } else {
                btnClubJoin.text = "Tham gia CLB"
                btnClubJoin.setBackgroundColor(Color.parseColor("#F4C073"))
                btnClubJoin.setTextColor(Color.parseColor("#121212"))
                Toast.makeText(this, "Đã rời khỏi $name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindData(name: String, subtitle: String, type: SearchType) {
        tvName.text = name
        tvSubtitle.text = subtitle

        if (type == SearchType.CLUB) {
            tvTopTitle.text = "Thông tin CLB"
            ivGroupIcon.visibility = View.VISIBLE
            ivAvatar.visibility = View.GONE
            layoutFriendSection.visibility = View.GONE
            layoutClubSection.visibility = View.VISIBLE
        } else {
            tvTopTitle.text = "Hồ sơ Runner"
            ivGroupIcon.visibility = View.GONE
            ivAvatar.visibility = View.VISIBLE
            ivAvatar.setImageResource(R.drawable.ic_person_chat)
            layoutFriendSection.visibility = View.VISIBLE
            layoutClubSection.visibility = View.GONE
        }
    }
}
