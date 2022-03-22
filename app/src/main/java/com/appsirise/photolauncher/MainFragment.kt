package com.appsirise.photolauncher

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appsirise.photolauncher.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var photoLauncherProvider: PhotoLauncherProvider
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoLauncherProvider = PhotoLauncherProvider()
        photoLauncherProvider.setupTakePhotoLauncher(
            this,
            ::doOnPhotoCaptureSuccess,
            ::doOnPhotoCaptureFail
        )
        photoLauncherProvider.setupGalleryPhotoLauncher(
            this,
            ::doOnPhotoSelectedSuccess,
            ::doOnPhotoSelectedFail
        )

        binding.capturePhoto.setOnClickListener { photoLauncherProvider.launchCapturePhoto(requireContext()) }
        binding.selectPhoto.setOnClickListener { photoLauncherProvider.launchGallerySelector() }
    }

    private fun doOnPhotoCaptureSuccess(fileUri: Uri) {
        binding.photo.setImageURI(fileUri)
    }

    private fun doOnPhotoCaptureFail() {
        Toast.makeText(requireContext(), "Error capture", Toast.LENGTH_LONG).show()
    }

    private fun doOnPhotoSelectedSuccess(fileUri: Uri?) {
        if (fileUri != null) {
            binding.photo.setImageURI(fileUri)
        } else {
            Toast.makeText(requireContext(), "Error selection 2", Toast.LENGTH_LONG).show()
        }
    }

    private fun doOnPhotoSelectedFail() {
        Toast.makeText(requireContext(), "Error selection 1", Toast.LENGTH_LONG).show()
    }
}