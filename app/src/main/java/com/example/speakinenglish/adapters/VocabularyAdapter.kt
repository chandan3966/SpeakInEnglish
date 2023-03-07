package com.example.speakinenglish.adapters

import android.content.Context
import android.media.MediaPlayer
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.api.model.Definitions
import com.example.api.model.Meanings
import com.example.api.model.Phonetics
import com.example.api.model.VocabInternalRes
import com.example.speakinenglish.R
import kotlinx.android.synthetic.main.vocabulary_list_view.view.*
import java.io.IOException


class VocabularyAdapter(
    private var context: Context,
    private var data: ArrayList<Any>
    ): RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder>() {

    class VocabularyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyViewHolder {
        return VocabularyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.vocabulary_list_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VocabularyViewHolder, position: Int) {
        if (itemCount > 0){
            var item = data.get(position)
            showLayouts(0,holder)
            when{
                (item is VocabInternalRes) -> {
                    showLayouts(1,holder)
                    var mainItem = item as VocabInternalRes
                    showView(1,holder,mainItem as Any)
                    holder.itemView.word.text = holder.itemView.word.text.toString()+mainItem.word
                    holder.itemView.origin.text = holder.itemView.origin.text.toString()+mainItem.origin
                    holder.itemView.phonetic.text = holder.itemView.phonetic.text.toString()+mainItem.phonetic
                    holder.itemView.sourceUrls.text = holder.itemView.sourceUrls.text.toString()+mainItem.sourceUrls.joinToString { "${HtmlCompat.fromHtml( "<a href=${it}>${it}</a>" ,HtmlCompat.FROM_HTML_MODE_COMPACT)}" }
                    var phoneticsAdapter = VocabularyAdapter(context,mainItem.phonetics as ArrayList<Any>)
                    holder.itemView.phoneticsRecycler.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
                    holder.itemView.phoneticsRecycler.adapter = phoneticsAdapter
                    var meaningsAdapter = VocabularyAdapter(context,mainItem.meanings as ArrayList<Any>)
                    holder.itemView.meaningsRecycler.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
                    holder.itemView.meaningsRecycler.adapter = meaningsAdapter

                }
                (item is Phonetics) -> {
                    showLayouts(2,holder)
                    var pheontic = item as Phonetics
                    showView(2,holder,pheontic as Any)
                    holder.itemView.phoneticText.text = holder.itemView.phoneticText.text.toString()+pheontic.text
                    holder.itemView.phoneticPlpay.setOnClickListener {
                        onRadioClick(pheontic.audio)
                    }
                }
                (item is Meanings) -> {
                    showLayouts(3,holder)
                    var meaning = item as Meanings
                    showView(3,holder,meaning as Any)
                    holder.itemView.speech.text = holder.itemView.speech.text.toString()+meaning.partOfSpeech
                    holder.itemView.synonyms.text = holder.itemView.synonyms.text.toString()+meaning.synonyms.joinToString { "${it}" }
                    holder.itemView.antonyms.text = holder.itemView.antonyms.text.toString()+meaning.antonyms.joinToString { "${it}" }
                    var definationAdapter = VocabularyAdapter(context,meaning.definitions as ArrayList<Any>)
                    holder.itemView.dictionaryRecycler.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
                    holder.itemView.dictionaryRecycler.adapter = definationAdapter
                }
                (item is Definitions) -> {
                    showLayouts(4,holder)
                    var defination = item as Definitions
                    showView(4,holder,defination as Any)
                    holder.itemView.defination.text = HtmlCompat.fromHtml( "<b>"+holder.itemView.defination.text.toString()+"</b>"+defination.definition ,HtmlCompat.FROM_HTML_MODE_COMPACT)
                    holder.itemView.example.text = HtmlCompat.fromHtml( "<b>"+holder.itemView.example.text.toString()+"</b>"+defination.example ,HtmlCompat.FROM_HTML_MODE_COMPACT)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun onRadioClick(url:String) {
        val mp = MediaPlayer()
        try {
            mp.setDataSource(url)
            mp.prepare()
            mp.start()
        } catch (e: IOException) {

        }
    }

    fun showLayouts(item:Int,holder: VocabularyViewHolder){
        when(item){
            1 -> {
                holder.itemView.mainLayer.visibility = View.VISIBLE
                holder.itemView.meaning.visibility = View.GONE
                holder.itemView.phonetics.visibility = View.GONE
                holder.itemView.definations.visibility = View.GONE
            }
            2 -> {
                holder.itemView.mainLayer.visibility = View.GONE
                holder.itemView.meaning.visibility = View.GONE
                holder.itemView.phonetics.visibility = View.VISIBLE
                holder.itemView.definations.visibility = View.GONE
            }
            3 -> {
                holder.itemView.mainLayer.visibility = View.GONE
                holder.itemView.meaning.visibility = View.VISIBLE
                holder.itemView.phonetics.visibility = View.GONE
                holder.itemView.definations.visibility = View.GONE
            }
            4 -> {
                holder.itemView.mainLayer.visibility = View.GONE
                holder.itemView.meaning.visibility = View.GONE
                holder.itemView.phonetics.visibility = View.GONE
                holder.itemView.definations.visibility = View.VISIBLE
            }
            else -> {
                holder.itemView.mainLayer.visibility = View.GONE
                holder.itemView.meaning.visibility = View.GONE
                holder.itemView.phonetics.visibility = View.GONE
                holder.itemView.definations.visibility = View.GONE
            }
        }
    }

    fun showView(item:Int,holder: VocabularyViewHolder,data: Any){
        when(item){
            1 -> {
                if (data is VocabInternalRes){
                    var item = data as VocabInternalRes
                    if (item.word.trim().equals("")) holder.itemView.word.visibility = View.GONE else holder.itemView.word.visibility = View.VISIBLE
                    if (item.origin.trim().equals("")) holder.itemView.origin.visibility = View.GONE else holder.itemView.origin.visibility = View.VISIBLE
                    if (item.phonetic.trim().equals("")) holder.itemView.phonetic.visibility = View.GONE else holder.itemView.phonetic.visibility = View.VISIBLE
                    if (item.sourceUrls.size == 0) holder.itemView.sourceUrls.visibility = View.GONE else holder.itemView.sourceUrls.visibility = View.VISIBLE
                    if (item.phonetics.size == 0) holder.itemView.phoneticsRecycler.visibility = View.GONE else holder.itemView.phoneticsRecycler.visibility = View.VISIBLE
                    if (item.meanings.size == 0) holder.itemView.meaningsRecycler.visibility = View.GONE else holder.itemView.meaningsRecycler.visibility = View.VISIBLE
                }
            }
            2 -> {
                if (data is Phonetics){
                    var item = data as Phonetics
                    if (item.text.trim().equals("")) holder.itemView.phoneticText.visibility = View.GONE else holder.itemView.phoneticText.visibility = View.VISIBLE
                    if (item.audio.trim().equals("")) holder.itemView.phoneticPlpay.visibility = View.GONE else holder.itemView.phoneticPlpay.visibility = View.VISIBLE
                }
            }
            3 -> {
                if (data is Meanings){
                    var item = data as Meanings
                    if (item.partOfSpeech.trim().equals("")) holder.itemView.speech.visibility = View.GONE else holder.itemView.speech.visibility = View.VISIBLE
                    if (item.synonyms.size == 0) holder.itemView.synonyms.visibility = View.GONE else holder.itemView.synonyms.visibility = View.VISIBLE
                    if (item.antonyms.size == 0) holder.itemView.antonyms.visibility = View.GONE else holder.itemView.antonyms.visibility = View.VISIBLE
                    if (item.definitions.size == 0) holder.itemView.dictionaryRecycler.visibility = View.GONE else holder.itemView.dictionaryRecycler.visibility = View.VISIBLE
                }
            }
            4 -> {
                if (data is Definitions){
                    var item = data as Definitions
                    if (item.definition.trim().equals("")) holder.itemView.defination.visibility = View.GONE else holder.itemView.defination.visibility = View.VISIBLE
                    if (item.example.trim().equals("")) holder.itemView.example.visibility = View.GONE else holder.itemView.example.visibility = View.VISIBLE
                }
            }
            else -> {

            }
        }
    }
}