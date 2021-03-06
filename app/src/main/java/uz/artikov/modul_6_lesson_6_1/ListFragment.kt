package uz.artikov.modul_6_lesson_6_1

import Adapter.RvAdapter
import Adapter.RvItemClick
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.fragment_list.view.*
import models.Music

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_list, container, false)

        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {

            val musicList: MutableList<Music> = context?.musicFiles()!!
            MyData.list = musicList as ArrayList

            val adapter = RvAdapter(musicList, object : RvItemClick {
                override fun itemClick(music: Music, position: Int) {
                    findNavController().navigate(
                        R.id.mediaFragment,
                        bundleOf("music" to music, "position" to position)
                    )
                }

            })

            root.rv.adapter = adapter

        }.onDeclined { e ->

            if (e.hasDenied()) {

                AlertDialog.Builder(context)
                    .setMessage("Please accept our permissions")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'

                // you need to open setting manually if you really need it
                e.goToSettings();
            }


        }

        return root
    }

    override fun onResume() {
        super.onResume()

        if(ContextCompat.checkSelfPermission(root.context,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            val musicList:MutableList<Music> = context?.musicFiles()!!
            MyData.list = musicList as ArrayList
            val adapter = RvAdapter(musicList,object :RvItemClick{
                override fun itemClick(music: Music, position: Int) {
                    findNavController().navigate(R.id.mediaFragment, bundleOf("music" to music, "position" to position))
                }

            })

            root.rv.adapter = adapter

        }
    }

    @SuppressLint("Range")
    fun Context.musicFiles(): MutableList<Music> {

        // Initialize an empty mutable list of music
        val list: MutableList<Music> = mutableListOf()

        // Get the external storage media store audio uri
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //val uri: Uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI

        // IS_MUSIC : Non-zero if the audio file is music
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"

        // Sort the musics
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        //val sortOrder = MediaStore.Audio.Media.TITLE + " DESC"

        // Query the external storage for music files
        val cursor: Cursor? = this.contentResolver.query(
            uri, // Uri
            null, // Projection
            selection, // Selection
            null, // Selection arguments
            sortOrder // Sort order
        )

        // If query result is not empty
        if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val title: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val imageId: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
            val authorId: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)

            // Now loop through the music files
            do {
                val audioId: Long = cursor.getLong(id)
                val audioTitle: String = cursor.getString(title)
                var imagePath: String = ""
                if (imageId != -1) {
                    imagePath = cursor.getString(imageId)
                }
                val musicPath: String =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val artist = cursor.getString(authorId)

                // Add the current music to the list
                list.add(Music(audioId, audioTitle, imagePath, musicPath, artist))
            } while (cursor.moveToNext())
        }

        // Finally, return the music files list
        return list

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

object MyData {

    var list: ArrayList<Music> = ArrayList()

}