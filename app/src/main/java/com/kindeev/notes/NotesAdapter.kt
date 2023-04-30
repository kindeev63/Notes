package com.kindeev.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.databinding.RcViewItemBinding
import com.kindeev.notes.db.Note

class NotesAdapter(private val onItemClick: (note: Note, long: Boolean) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NotesHolder>() {
    var notesList = emptyList<Note>()

    class NotesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = RcViewItemBinding.bind(view)
        fun bind(note: Note) = with(binding) {
            tTitle.text = note.title
            tTime.text = note.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NotesHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.rc_view_item, parent, false)
    )

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        holder.bind(notesList[position])
        holder.itemView.setOnClickListener {
            onItemClick(notesList[position], false)
        }
        holder.itemView.setOnLongClickListener {
            onItemClick(notesList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(data: List<Note>) {
        notesList = data
        notifyDataSetChanged()
    }
}