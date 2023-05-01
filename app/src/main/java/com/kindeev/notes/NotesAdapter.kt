package com.kindeev.notes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.databinding.NoteItemBinding
import com.kindeev.notes.db.Note

class NotesAdapter(private val onItemClick: (note: Note, open: Boolean) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NotesHolder>() {
    private var notesList = emptyList<Note>()
    val selectedNotes = arrayListOf<Note>()

    class NotesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = NoteItemBinding.bind(view)
        fun bind(note: Note, choosingNotes: Boolean, noteSelected: Boolean) = with(binding) {
            tTitle.text = note.title
            tTime.text = note.time
            chDelete.visibility =
            if (choosingNotes){
                View.VISIBLE
            } else View.GONE
            chDelete.isChecked = noteSelected

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NotesHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
    )

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        holder.bind(notesList[position], selectedNotes.size > 0, notesList[position] in selectedNotes)
        holder.itemView.setOnClickListener {
            val open = selectedNotes.size == 0
            if (selectedNotes.size > 0){
                val note = notesList[position]
                if (note in selectedNotes) selectedNotes.remove(note)
                else selectedNotes.add(note)
                notifyDataSetChanged()
            }
            onItemClick(notesList[position], open)
        }
        holder.itemView.setOnLongClickListener {
            val note = notesList[position]
            if (note in selectedNotes) selectedNotes.remove(note)
                else selectedNotes.add(note)
            notifyDataSetChanged()
            onItemClick(note, true)
            return@setOnLongClickListener true
        }
    }

    fun setData(data: List<Note>) {
        notesList = data
        notifyDataSetChanged()
    }
}