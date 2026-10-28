package com.example.runtrack.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.runtrack.R

/**
 * [SCR-PRIV-02] Friend Requests & Social Discovery Settings Screen.
 */
class FriendRequestsFragment : Fragment() {

    private lateinit var cardEveryone: View
    private lateinit var radioEveryone: TextView
    private lateinit var cardFof: View
    private lateinit var radioFof: TextView
    private lateinit var cardNoOne: View
    private lateinit var radioNoOne: TextView

    private lateinit var switchSearchContact: SwitchCompat
    private lateinit var switchSuggestClub: SwitchCompat
    private lateinit var switchAutoAcceptClub: SwitchCompat
    private lateinit var btnSave: TextView

    private var currentScope = FriendRequestScope.EVERYONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hud_friend_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadPreferences()
        setupListeners(view)
        updateRadioUI()
    }

    private fun initViews(view: View) {
        cardEveryone = view.findViewById(R.id.card_req_everyone)
        radioEveryone = view.findViewById(R.id.radio_everyone_indicator)
        cardFof = view.findViewById(R.id.card_req_fof)
        radioFof = view.findViewById(R.id.radio_fof_indicator)
        cardNoOne = view.findViewById(R.id.card_req_no_one)
        radioNoOne = view.findViewById(R.id.radio_no_one_indicator)

        switchSearchContact = view.findViewById(R.id.switch_allow_search_contact)
        switchSuggestClub = view.findViewById(R.id.switch_suggest_club)
        switchAutoAcceptClub = view.findViewById(R.id.switch_auto_accept_club)
        btnSave = view.findViewById(R.id.btnSaveFriendRequests)
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_friend_requests", Context.MODE_PRIVATE)
        val savedScopeName = prefs.getString("scope", FriendRequestScope.EVERYONE.name)
        currentScope = try {
            FriendRequestScope.valueOf(savedScopeName ?: FriendRequestScope.EVERYONE.name)
        } catch (_: Exception) {
            FriendRequestScope.EVERYONE
        }
        switchSearchContact.isChecked = prefs.getBoolean("search_contact", true)
        switchSuggestClub.isChecked = prefs.getBoolean("suggest_club", true)
        switchAutoAcceptClub.isChecked = prefs.getBoolean("auto_accept_club", false)
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        cardEveryone.setOnClickListener {
            currentScope = FriendRequestScope.EVERYONE
            updateRadioUI()
        }
        cardFof.setOnClickListener {
            currentScope = FriendRequestScope.FRIENDS_OF_FRIENDS
            updateRadioUI()
        }
        cardNoOne.setOnClickListener {
            currentScope = FriendRequestScope.NO_ONE
            updateRadioUI()
        }

        btnSave.setOnClickListener {
            savePreferences()
        }
    }

    private fun updateRadioUI() {
        val context = context ?: return
        val activeColor = ContextCompat.getColor(context, R.color.kinetic_orange)
        val dimColor = ContextCompat.getColor(context, R.color.hud_text_dim)

        radioEveryone.text = if (currentScope == FriendRequestScope.EVERYONE) "●" else "○"
        radioEveryone.setTextColor(if (currentScope == FriendRequestScope.EVERYONE) activeColor else dimColor)

        radioFof.text = if (currentScope == FriendRequestScope.FRIENDS_OF_FRIENDS) "●" else "○"
        radioFof.setTextColor(if (currentScope == FriendRequestScope.FRIENDS_OF_FRIENDS) activeColor else dimColor)

        radioNoOne.text = if (currentScope == FriendRequestScope.NO_ONE) "●" else "○"
        radioNoOne.setTextColor(if (currentScope == FriendRequestScope.NO_ONE) activeColor else dimColor)
    }

    private fun savePreferences() {
        val prefs = requireContext().getSharedPreferences("runtrack_friend_requests", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("scope", currentScope.name)
            .putBoolean("search_contact", switchSearchContact.isChecked)
            .putBoolean("suggest_club", switchSuggestClub.isChecked)
            .putBoolean("auto_accept_club", switchAutoAcceptClub.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Đã lưu thiết lập kết bạn thành công! ✓", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
}
