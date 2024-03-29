package com.infinity.attendance.view.ui.individual_user.leave_report;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.infinity.attendance.R;
import com.infinity.attendance.data.model.Leave;
import com.infinity.attendance.data.model.LeaveType;
import com.infinity.attendance.data.model.User;
import com.infinity.attendance.utils.ConstantKey;
import com.infinity.attendance.utils.OnDataUpdateListener;
import com.infinity.attendance.view.adapter.AdapterDialogLeaveType;
import com.infinity.attendance.viewmodel.DataViewModel;
import com.infinity.attendance.viewmodel.repo.ApiResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import render.animations.Attention;
import render.animations.Render;

public class ApplyLeaveDialog extends DialogFragment {
    public static final String TAG = "AddAdminDialog";
    // Get Current Date
    final Calendar c = Calendar.getInstance();
    AutoCompleteTextView tv_leaveType;
    AutoCompleteTextView fromDate, toDate;
    TextInputEditText appyingDate;
    int cYear = c.get(Calendar.YEAR);
    int cMonth = c.get(Calendar.MONTH);
    int cDay = c.get(Calendar.DAY_OF_MONTH);
    private OnDataUpdateListener<List<Leave>> onDataUpdateListener;
    private AlertDialog alert;
    private LeaveType selectedLeaveType;
    //
    private List<LeaveType> leaveTypeList = new ArrayList<>();

    private OnDialogItemClick onDialogItemClick;
    private TextInputEditText inputPurpose;
    private String stringUser;
    private User selectedUser;
    private TextInputLayout fromDateLayout, toDateLayout, leaveTypeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        if (getArguments() != null) {
            stringUser = getArguments().getString(ConstantKey.SELECTED_USER, null);
            selectedUser = new Gson().fromJson(stringUser, User.class);
        }
    }

    public void setOnDataUpdateListener(OnDataUpdateListener<List<Leave>> onDataUpdateListener) {
        this.onDataUpdateListener = onDataUpdateListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_apply_leave, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fabBack = view.findViewById(R.id.fabBack);
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApplyLeaveDialog.this.dismiss();
            }
        });

        getLeaveTypeList();

        appyingDate = view.findViewById(R.id.currentDate);

        Date date = c.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        appyingDate.setText(dateFormat.format(date));
        appyingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth);
                                Date date = calendar.getTime();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                appyingDate.setText(dateFormat.format(date));
                            }
                        }, cYear, cMonth, cDay).show();
            }
        });
        //
        inputPurpose = view.findViewById(R.id.purpose);
        //
        fromDateLayout = view.findViewById(R.id.fromDateLayout);
        fromDate = view.findViewById(R.id.fromDate);
        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth);
                                Date date = calendar.getTime();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                fromDate.setText(dateFormat.format(date));
                            }
                        }, cYear, cMonth, cDay).show();
            }
        });
        //
        toDateLayout = view.findViewById(R.id.toDateLayout);
        toDate = view.findViewById(R.id.toDate);
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth);
                                Date date = calendar.getTime();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                toDate.setText(dateFormat.format(date));

                            }
                        }, cYear, cMonth, cDay).show();
            }
        });
        //
        leaveTypeLayout = view.findViewById(R.id.leaveTypeLayout);
        tv_leaveType = view.findViewById(R.id.leaveType);
        tv_leaveType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onDialogItemClick = new OnDialogItemClick() {
                    @Override
                    public void onDialogItemClicked(LeaveType leaveType) {
                        selectedLeaveType = leaveType;
                        Log.d(TAG, "onDialogItemClicked: " + selectedLeaveType.getId());
                        Log.d(TAG, "onDialogItemClicked: " + leaveType.getId());
                        tv_leaveType.setText(leaveType.getName());
                        if (alert.isShowing()) {
                            alert.dismiss();
                        }
                    }
                };

                alert = _createLeaveTypeDialog(leaveTypeList, onDialogItemClick);
                alert.show();

            }
        });
        //
        FloatingActionButton fabSubmitLeave = view.findViewById(R.id.fabSubmitLeave);
        fabSubmitLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 8/31/2020 save application to database
                if (isValid()) {

                    Leave leave = new Leave();
                    leave.setUid(selectedUser.getUid());
                    leave.setApply_date(appyingDate.getText().toString());
                    leave.setPurpose(inputPurpose.getText().toString());
                    leave.setFrom_date(fromDate.getText().toString());
                    leave.setTo_date(toDate.getText().toString());
                    leave.setType_id(selectedLeaveType.getId());

                    DataViewModel dataViewModel = new DataViewModel();
                    dataViewModel.applyLeave(leave).observe(getViewLifecycleOwner(),
                            new Observer<ApiResponse<Leave>>() {
                                @Override
                                public void onChanged(ApiResponse<Leave> leaveApiResponse) {
                                    if (leaveApiResponse != null) {
                                        ApplyLeaveDialog.this.dismiss();
                                        onDataUpdateListener.onSuccessfulDataUpdated(leaveApiResponse.getData());
                                    } else {
                                        Toast.makeText(getContext(), getContext().getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                    );

                }
            }
        });


    }

    private boolean isValid() {

        if (TextUtils.isEmpty(appyingDate.getText())) {
            appyingDate.setError("can't be empty");
            return false;
        }
        if (TextUtils.isEmpty(inputPurpose.getText())) {
            inputPurpose.setError("can't be empty");
            return false;
        }

        if (TextUtils.isEmpty(fromDate.getText())) {
            Render render = new Render(getContext());
            render.setAnimation(Attention.Wobble(fromDateLayout));
            render.start();
            return false;
        }

        if (TextUtils.isEmpty(toDate.getText())) {
            Render render = new Render(getContext());
            render.setAnimation(Attention.Wobble(toDateLayout));
            render.start();
            return false;
        }

        if (selectedLeaveType.getId() < 1) {
            Render render = new Render(getContext());
            render.setAnimation(Attention.Wobble(leaveTypeLayout));
            render.start();
            return false;
        }

        return true;
    }

    private void getLeaveTypeList() {

        DataViewModel dataViewModel = new DataViewModel();
        dataViewModel.getLeaveType().observe(getViewLifecycleOwner(), new Observer<ApiResponse<LeaveType>>() {
            @Override
            public void onChanged(ApiResponse<LeaveType> leaveTypeApiResponse) {
                if (leaveTypeApiResponse != null) {
                    leaveTypeList = leaveTypeApiResponse.getData();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private AlertDialog _createLeaveTypeDialog(List<LeaveType> leaveTypeList, OnDialogItemClick onDialogItemClick) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_role, null);
        ListView listView = view.findViewById(R.id.list);
        AdapterDialogLeaveType adapter = new AdapterDialogLeaveType(getContext(), leaveTypeList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                onDialogItemClick.onDialogItemClicked(leaveTypeList.get(i));
                Log.d(TAG, "onItemClick: " + leaveTypeList.get(i));
            }
        });

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setView(view);
        return alertDialog.create();

    }


    interface OnDialogItemClick {
        void onDialogItemClicked(LeaveType leaveType);
    }

}
