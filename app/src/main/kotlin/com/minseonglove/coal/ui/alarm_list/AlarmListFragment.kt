package com.minseonglove.coal.ui.alarm_list

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.minseonglove.coal.R
import com.minseonglove.coal.databinding.FragmentAlarmListBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmListFragment : Fragment() {

    private lateinit var _binding: FragmentAlarmListBinding

    private val binding get() = _binding
    private val viewModel: AlarmListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate<FragmentAlarmListBinding>(
            inflater,
            R.layout.fragment_alarm_list,
            container,
            false
        ).apply {
            vm = viewModel
            lifecycleOwner = this@AlarmListFragment
        }
        Logger.addLogAdapter(AndroidLogAdapter())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarAlarmlist.buttonToolbarNavigation.setOnClickListener {
            Toast.makeText(requireContext(), "asdf", Toast.LENGTH_SHORT).show()
        }
    }
}