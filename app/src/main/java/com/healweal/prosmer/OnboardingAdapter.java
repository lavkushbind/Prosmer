package com.healweal.prosmer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 5;

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return OnboardingSlideFragment.newInstance(R.string.onboarding_title_1, R.string.onboarding_desc_1, R.raw.loading_1);
            case 1:
                return OnboardingSlideFragment.newInstance(R.string.onboarding_title_2, R.string.onboarding_desc_2, R.raw.onboarding_1);
            case 2:
                return OnboardingSlideFragment.newInstance(R.string.onboarding_title_3, R.string.onboarding_desc_3, R.raw.onboreding_3);
            case 3:
                return OnboardingSlideFragment.newInstance(R.string.onboarding_title_4, R.string.onboarding_desc_4, R.raw.onboreding_4
                );
            case 4:
                return OnboardingSlideFragment.newInstance(R.string.onboarding_title_5, R.string.onboarding_desc_5, R.raw.onboreding_5);
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}