package com.example

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.view.Choreographer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class SystemMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _cpuTemp = MutableStateFlow<Float?>(null)
    val cpuTemp: StateFlow<Float?> = _cpuTemp.asStateFlow()

    private val _memoryUsage = MutableStateFlow(0f) // Percentage
    val memoryUsage: StateFlow<Float> = _memoryUsage.asStateFlow()

    private val _usedMemGb = MutableStateFlow(0f)
    val usedMemGb: StateFlow<Float> = _usedMemGb.asStateFlow()

    private val _totalMemGb = MutableStateFlow(0f)
    val totalMemGb: StateFlow<Float> = _totalMemGb.asStateFlow()

    private var lastFrameTimeNanos: Long = 0
    private var frameCount = 0

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (lastFrameTimeNanos == 0L) {
                lastFrameTimeNanos = frameTimeNanos
            } else {
                val diffMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                frameCount++
                if (diffMs >= 1000) {
                    val currentFps = (frameCount * 1000f / diffMs).toInt()
                    _fps.value = currentFps
                    frameCount = 0
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        Choreographer.getInstance().postFrameCallback(frameCallback)
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                updateMemoryUsage()
                updateCpuTemp()
                delay(1000) // Update every second
            }
        }
    }

    private fun updateMemoryUsage() {
        val activityManager = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMem = memoryInfo.totalMem
        val availMem = memoryInfo.availMem
        val usedMem = totalMem - availMem

        _memoryUsage.value = if (totalMem > 0) (usedMem.toFloat() / totalMem.toFloat()) else 0f
        
        _usedMemGb.value = usedMem / (1024f * 1024f * 1024f)
        _totalMemGb.value = totalMem / (1024f * 1024f * 1024f)
    }

    private fun updateCpuTemp() {
        val paths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/class/hwmon/hwmon0/device/temp1_input",
            "/sys/class/hwmon/hwmon1/device/temp1_input",
            "/sys/class/hwmon/hwmon2/device/temp1_input"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val tempStr = file.readText().trim()
                    val tempInt = tempStr.toIntOrNull()
                    if (tempInt != null) {
                        // Some return in millidegrees, some in degrees
                        _cpuTemp.value = if (tempInt > 1000) tempInt / 1000f else tempInt.toFloat()
                        return
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        _cpuTemp.value = null // Unknown
    }

    override fun onCleared() {
        super.onCleared()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
