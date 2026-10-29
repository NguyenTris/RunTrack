package com.example.runtrack.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runtrack.R

class LoggedInDevicesAdapter(
    private var devices: MutableList<LoggedInDevice>,
    private val onLogoutClick: (LoggedInDevice, Int) -> Unit
) : RecyclerView.Adapter<LoggedInDevicesAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logged_in_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position], position)
    }

    override fun getItemCount(): Int = devices.size

    fun removeDeviceAt(position: Int) {
        if (position in devices.indices) {
            devices.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, devices.size - position)
        }
    }

    fun clearAll() {
        val size = devices.size
        devices.clear()
        notifyItemRangeRemoved(0, size)
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_device_icon)
        private val tvName: TextView = itemView.findViewById(R.id.tv_device_name)
        private val tvClient: TextView = itemView.findViewById(R.id.tv_device_client)
        private val tvIpTime: TextView = itemView.findViewById(R.id.tv_device_ip_time)
        private val btnLogout: TextView = itemView.findViewById(R.id.btn_logout_device)

        fun bind(device: LoggedInDevice, position: Int) {
            tvName.text = device.name
            tvClient.text = "${device.clientApp} · ${device.location}"
            tvIpTime.text = "IP: ${device.ipAddress} · ${device.lastActive}"

            // Icon mapping
            when (device.deviceType) {
                DeviceType.MOBILE -> ivIcon.setImageResource(R.drawable.ic_device_phone)
                DeviceType.DESKTOP -> ivIcon.setImageResource(R.drawable.ic_device_laptop)
                DeviceType.TABLET -> ivIcon.setImageResource(R.drawable.ic_device_tablet)
                DeviceType.WATCH -> ivIcon.setImageResource(R.drawable.ic_bluetooth)
            }

            btnLogout.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos in devices.indices) {
                    onLogoutClick(devices[pos], pos)
                }
            }
        }
    }
}
