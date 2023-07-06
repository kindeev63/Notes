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

class NotesAdapter(private val onItemClick: (note: Note, long: Boolean) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NotesHolder>() {
    private var notesList = emptyList<Note>()
    private var selectedNotesList = emptyList<Note>()

    class NotesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = NoteItemBinding.bind(view)
        fun bind(note: Note, selectedNotes: List<Note>) = with(binding) {
            tTitle.text = note.title
            val formatter = SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault())
            val formattedDateTime = formatter.format(note.time)
            tTime.text = formattedDateTime
            noteContent.setBackgroundColor(note.color)
            chDeleteNote.visibility =
                if (selectedNotes.isEmpty()) {
                    View.GONE
                } else {
                    chDeleteNote.isChecked = note in selectedNotes
                    View.VISIBLE
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NotesHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
    )

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        holder.bind(
            notesList[position],
            selectedNotesList
        )
        holder.itemView.setOnClickListener {
            onItemClick(notesList[position], false)
        }
        holder.itemView.setOnLongClickListener {
            onItemClick(notesList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(notes: List<Note>? = null, selectedNotes: List<Note>? = null) {
        notes?.let { notesList = it }
        selectedNotes?.let { selectedNotesList = it }
        notifyDataSetChanged()
    }
}