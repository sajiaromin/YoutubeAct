package com.example.youtubeact

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.example.youtubeact.handler.VideoHandler
import com.example.youtubeact.model.Video
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    lateinit var titleET: EditText
    lateinit var linkET: EditText
    lateinit var rankET: EditText
    lateinit var reasonET: EditText
    lateinit var addEditButton: Button
    lateinit var videoHandler: VideoHandler
    lateinit var videos: ArrayList<Video>
    lateinit var videoListView: ListView
    lateinit var videoGettingEdited: Video



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleET = findViewById(R.id.titleET)
        linkET = findViewById(R.id.linkET)
        rankET = findViewById(R.id.rankET)
        reasonET = findViewById(R.id.reasonET)
        addEditButton = findViewById(R.id.addEditButton)
        videoHandler = VideoHandler()

        videos = ArrayList()
        videoListView=findViewById(R.id.videosListView)

        addEditButton.setOnClickListener{
            val title = titleET.text.toString()
            val link = linkET.text.toString()
            val rank = rankET.text.toString()
            val reason = reasonET.text.toString()
            try {
                rank.toInt()
                if (addEditButton.text.toString() == "Add"){
                    val video = Video(title = title, link = link, rank = rank, reason = reason)
                    if (videoHandler.create(video)) {
                        Toast.makeText(applicationContext, "Video added", Toast.LENGTH_SHORT).show()
                        clearFields()
                    }
                }
                else if(addEditButton.text.toString() == "Update"){
                    val video = Video(id=videoGettingEdited.id, title=title, link=link, rank=rank, reason=reason)
                    if(videoHandler.update(video)){
                        Toast.makeText(applicationContext, "Video updated", Toast.LENGTH_SHORT).show()
                        clearFields()
                    }
                }
            } catch(e: NumberFormatException) {
                Toast.makeText(applicationContext, "Please enter a number for the ranking.", Toast.LENGTH_SHORT).show()
                this
            }

        }
        registerForContextMenu(videoListView)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.video_options,menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when(item.itemId){
            R.id.edit_video->{
                videoGettingEdited = videos[info.position]
                titleET.setText(videoGettingEdited.title)
                linkET.setText(videoGettingEdited.link)
                rankET.setText(videoGettingEdited.rank)
                reasonET.setText(videoGettingEdited.reason)
                addEditButton.setText("Update")
                true
            }
            R.id.delete_video ->{
                if(videoHandler.delete(videos[info.position])){
                    Toast.makeText(applicationContext, "Video deleted", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onContextItemSelected(item)

        }

    }


    override fun onStart(){
        super.onStart()
        videoHandler.videoRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                videos.clear()
                snapshot.children.forEach{
                        it-> val video = it.getValue(Video::class.java)
                    videos.add(video!!)
                }

                videos.sortBy { it.rank!!.toInt() }

                val adapter = ArrayAdapter<Video>(applicationContext, android.R.layout.simple_list_item_1, videos)
                videoListView.adapter=adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }
    fun clearFields(){
        titleET.text.clear()
        linkET.text.clear()
        rankET.text.clear()
        reasonET.text.clear()
        addEditButton.setText("Add")
    }

}