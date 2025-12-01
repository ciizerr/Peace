package com.nami.peace.ui.reminder

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.Attachment
import com.nami.peace.domain.model.Note
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.usecase.AddAttachmentUseCase
import com.nami.peace.domain.usecase.AddNoteUseCase
import com.nami.peace.domain.usecase.DeleteAttachmentUseCase
import com.nami.peace.domain.usecase.DeleteNoteUseCase
import com.nami.peace.domain.usecase.GetAttachmentsForReminderUseCase
import com.nami.peace.domain.usecase.GetNotesForReminderUseCase
import com.nami.peace.util.deeplink.DeepLinkHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderDetailUiState(
    val reminder: Reminder? = null,
    val notes: List<Note> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val isLoading: Boolean = true,
    val showAddNoteDialog: Boolean = false,
    val showImagePickerDialog: Boolean = false,
    val selectedAttachment: Attachment? = null,
    val shareLink: String? = null,
    val showShareConfirmation: Boolean = false
)

@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val getNotesForReminderUseCase: GetNotesForReminderUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getAttachmentsForReminderUseCase: GetAttachmentsForReminderUseCase,
    private val addAttachmentUseCase: AddAttachmentUseCase,
    private val deleteAttachmentUseCase: DeleteAttachmentUseCase,
    private val deepLinkHandler: DeepLinkHandler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = mutableStateOf(ReminderDetailUiState())
    val uiState: State<ReminderDetailUiState> = _uiState
    
    private var currentReminderId: Int? = null

    init {
        val reminderId = savedStateHandle.get<Int>("reminderId")
        if (reminderId != null && reminderId != -1) {
            currentReminderId = reminderId
            loadReminder(reminderId)
            loadNotes(reminderId)
            loadAttachments(reminderId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadReminder(id: Int) {
        viewModelScope.launch {
            val reminder = repository.getReminderById(id)
            _uiState.value = _uiState.value.copy(
                reminder = reminder,
                isLoading = false
            )
        }
    }
    
    private fun loadNotes(reminderId: Int) {
        viewModelScope.launch {
            getNotesForReminderUseCase(reminderId).collectLatest { notes ->
                _uiState.value = _uiState.value.copy(notes = notes)
            }
        }
    }
    
    private fun loadAttachments(reminderId: Int) {
        viewModelScope.launch {
            getAttachmentsForReminderUseCase(reminderId).collectLatest { attachments ->
                _uiState.value = _uiState.value.copy(attachments = attachments)
            }
        }
    }
    
    fun showAddNoteDialog() {
        _uiState.value = _uiState.value.copy(showAddNoteDialog = true)
    }
    
    fun hideAddNoteDialog() {
        _uiState.value = _uiState.value.copy(showAddNoteDialog = false)
    }
    
    fun addNote(content: String) {
        val reminderId = currentReminderId ?: return
        viewModelScope.launch {
            try {
                addNoteUseCase(reminderId, content)
                hideAddNoteDialog()
            } catch (e: Exception) {
                // Handle error - could add error state to UI
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(note)
            } catch (e: Exception) {
                // Handle error - could add error state to UI
            }
        }
    }
    
    fun showImagePickerDialog() {
        _uiState.value = _uiState.value.copy(showImagePickerDialog = true)
    }
    
    fun hideImagePickerDialog() {
        _uiState.value = _uiState.value.copy(showImagePickerDialog = false)
    }
    
    fun addAttachment(imageUri: Uri) {
        val reminderId = currentReminderId ?: return
        viewModelScope.launch {
            try {
                addAttachmentUseCase(imageUri, reminderId)
                hideImagePickerDialog()
            } catch (e: Exception) {
                // Handle error - could add error state to UI
            }
        }
    }
    
    fun deleteAttachment(attachment: Attachment) {
        viewModelScope.launch {
            try {
                deleteAttachmentUseCase(attachment)
            } catch (e: Exception) {
                // Handle error - could add error state to UI
            }
        }
    }
    
    fun showFullScreenImage(attachment: Attachment) {
        _uiState.value = _uiState.value.copy(selectedAttachment = attachment)
    }
    
    fun hideFullScreenImage() {
        _uiState.value = _uiState.value.copy(selectedAttachment = null)
    }
    
    /**
     * Generates a deep link for sharing the current reminder.
     * Returns the deep link string that can be shared via Android share sheet.
     */
    fun generateShareLink(): String? {
        val reminder = _uiState.value.reminder ?: return null
        return try {
            val shareLink = deepLinkHandler.createShareLink(reminder)
            _uiState.value = _uiState.value.copy(shareLink = shareLink)
            shareLink
        } catch (e: Exception) {
            // Handle error - data too large or encoding failed
            null
        }
    }
    
    /**
     * Shows the share confirmation toast.
     */
    fun showShareConfirmation() {
        _uiState.value = _uiState.value.copy(showShareConfirmation = true)
    }
    
    /**
     * Hides the share confirmation toast.
     */
    fun hideShareConfirmation() {
        _uiState.value = _uiState.value.copy(showShareConfirmation = false)
    }
}
