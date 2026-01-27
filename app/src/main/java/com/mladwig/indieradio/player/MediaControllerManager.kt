package com.mladwig.indieradio.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mladwig.indieradio.service.RadioPlaybackService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class MediaControllerManager(private val context : Context) {
    private var controllerFuture : ListenableFuture<MediaController>? = null
    private var controller : MediaController? = null

    suspend fun getController(): MediaController {
        //Return existing controller (if we have one)
        controller?.let {return it}

        //Otherwise, connect to the service
        return suspendCancellableCoroutine { continuation ->
            val sessionToken = SessionToken(
                context,
                ComponentName(context, RadioPlaybackService::class.java)
            )

            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            controllerFuture?.addListener({
                controller = controllerFuture?.get()
                continuation.resume(controller!!)
            }, MoreExecutors.directExecutor())
        }

    }

    fun release() {
        controller?.release()
        controller = null
        controllerFuture = null
    }
}