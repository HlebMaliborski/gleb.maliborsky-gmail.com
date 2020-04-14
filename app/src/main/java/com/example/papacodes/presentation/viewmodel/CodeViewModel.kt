package com.example.papacodes.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.core_common.result.Failure
import com.example.papacodes.common.response.Either
import com.example.papacodes.domain.model.DomainCodeModel
import com.example.papacodes.domain.usecase.BaseUseCase
import com.example.papacodes.domain.usecase.GetAllCodeUseCase
import com.example.papacodes.domain.usecase.GetAllFilteredCodes
import com.example.papacodes.domain.usecase.Params
import com.example.papacodes.presentation.mapper.DomainToPresentationMapper
import com.example.papacodes.presentation.model.PresentationCodeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CodeViewModel(
    private val mapper: DomainToPresentationMapper,
    private val getAllCodeUseCase: GetAllCodeUseCase,
    private val getAllCodesFilteredByCity: GetAllFilteredCodes
) : BaseViewModel() {
    private val _viewState = liveData {
        emit(ViewState())
        val state = handleResult(getAllCodeUseCase.invoke(BaseUseCase.None())).copy(
            initializeView = true
        )
        emit(state)
    } as MutableLiveData
    val viewState: LiveData<ViewState> = _viewState

    private val filterMap: MutableMap<String, String> = mutableMapOf()

    init {
        filterMap[CITY] = RESET
        filterMap[SIZE] = RESET
        filterMap[PRICE] = RESET
    }

    fun onFilter(type: String, value: String) {
        filterMap[type] = value
        viewModelScope.launch {
            _viewState.value = handleResult(getAllCodesFilteredByCity.invoke(Params(filterMap)))
        }
    }

    private fun currentViewState(): ViewState = _viewState.value!!

    private fun handleResult(result: Either<Failure, DomainCodeModel>): ViewState {
        return when (result) {
            is Either.Error -> currentViewState().copy(
                isLoading = false,
                failure = result.a,
                initializeView = false
            )
            is Either.Success -> currentViewState().copy(
                isLoading = false,
                failure = Failure.None,
                presentationModel = mapper.map(result.data),
                initializeView = false
            )
        }
    }

    data class ViewState(
        val isLoading: Boolean = true,
        val failure: Failure = Failure.None,
        val presentationModel: PresentationCodeModel? = null,
        val initializeView: Boolean = false
    )

    companion object {
        const val CITY = "city"
        const val SIZE = "size"
        const val PRICE = "price"
        const val RESET = "reset"
    }
}