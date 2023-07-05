package com.kindeev.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.R
import com.kindeev.notes.databinding.NoteItemBinding
import com.kindeev.notes.db.Note
import java.text.SimpleDateFormat
import java.util.*

class PickNoteAdapter(private val onItemClick: (note: Note) -> Unit) :
    RecyclerView.Adapter<PickNoteAdapter.PickNotesHolder>() {
    private var notesList = emptyList<Note>()

    class PickNotesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = NoteItemBinding.bind(view)
        fun bind(note: Note) = with(binding) {
            tTitle.text = note.title
            val formatter = SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault())
            val formattedDateTime = formatter.format(note.time)
            tTime.text = formattedDateTime
            noteContent.setBackgroundColor(note.color)
            chDeleteNote.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PickNotesHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
    )

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: PickNotesHolder, position: Int) {
        holder.bind(notesList[position])
        holder.itemView.setOnClickListener {
            onItemClick(notesList[position])
        }
    }

    fun setData(notes: List<Note>) {
        notesList = notes
        notifyDataSetChanged()
    }
}