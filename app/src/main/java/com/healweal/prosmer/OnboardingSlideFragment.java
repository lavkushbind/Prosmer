package com.healweal.prosmer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;

public class OnboardingSlideFragment extends Fragment {

    private static final String ARG_TITLE_RES = "arg_title_res";
    private static final String ARG_DESC_RES = "arg_desc_res";
    private static final String ARG_LOTTIE_RES = "arg_lottie_res";

    public static OnboardingSlideFragment newInstance(int titleRes, int descRes, int lottieRes) {
        OnboardingSlideFragment fragment = new OnboardingSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_RES, titleRes);
        args.putInt(ARG_DESC_RES, descRes);
        args.putInt(ARG_LOTTIE_RES, lottieRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_slide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textTitle = view.findViewById(R.id.text_title);
        TextView textDescription = view.findViewById(R.id.text_description);
        LottieAnimationView lottieAnimation = view.findViewById(R.id.lottie_animation);

        if (getArguments() != null) {
            textTitle.setText(getString(getArguments().getInt(ARG_TITLE_RES)));
            textDescription.setText(getString(getArguments().getInt(ARG_DESC_RES)));
            lottieAnimation.setAnimation(getArguments().getInt(ARG_LOTTIE_RES));
        }
    }
}