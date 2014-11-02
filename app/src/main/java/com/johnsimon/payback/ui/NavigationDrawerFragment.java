package com.johnsimon.payback.ui;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.johnsimon.payback.adapter.NavigationDrawerAdapter;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.util.RobotoMediumTextView;
import com.johnsimon.payback.util.Resource;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    public ActionBarDrawerToggle mDrawerToggle;

	public static NavigationDrawerAdapter adapter;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private ImageButton headerArrow;
    private LinearLayout headerTextContainer;
    private RobotoMediumTextView headerName;
    private static RobotoMediumTextView headerPlus;
    private static RobotoMediumTextView headerMinus;

    public static int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
	private boolean inHeaderDetailScreen = false;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		        selectItem(position);
	        }
        });
	    adapter = new NavigationDrawerAdapter(getActivity(), Resource.people);

		setSelectedPerson(FeedActivity.person);
		selectItem(mCurrentSelectedPosition);

        mDrawerListView.setAdapter(adapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

		mDrawerListView.addFooterView(inflater.inflate(R.layout.navigation_drawer_list_footer, null));

		View headerView = inflater.inflate(R.layout.navigation_drawer_list_header, null);

		headerTextContainer = (LinearLayout) headerView.findViewById(R.id.navigation_drawer_header_text_container);
		headerName = (RobotoMediumTextView) headerView.findViewById(R.id.navigation_drawer_header_name);
		headerPlus = (RobotoMediumTextView) headerView.findViewById(R.id.navigation_drawer_header_plus);
		headerMinus = (RobotoMediumTextView) headerView.findViewById(R.id.navigation_drawer_header_minus);
		headerArrow = (ImageButton) headerView.findViewById(R.id.navigation_drawer_header_arrow);

		updateBalance();
		updateName();

		headerTextContainer.setTranslationY(Resource.getPx(58, getActivity().getResources()));
        headerPlus.setAlpha(0f);
        headerMinus.setAlpha(0f);

		headerArrow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                handleArrowRotation();
			}
		});

        headerTextContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleArrowRotation();
            }
        });


		mDrawerListView.addHeaderView(headerView);

        return mDrawerListView;
    }

    public void handleArrowRotation() {
        if (inHeaderDetailScreen) {
            //Spin to down arrow

			headerArrow.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerTextContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerPlus.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerMinus.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            headerArrow.setRotation(180f);

            ObjectAnimator rotation = ObjectAnimator.ofFloat(headerArrow,
                    "rotation", 360f);
            rotation.setDuration(300);
            rotation.start();

            headerTextContainer.setTranslationY(0);

            ObjectAnimator animY = ObjectAnimator.ofFloat(headerTextContainer, "translationY", Resource.getPx(58, getActivity().getResources()));
            animY.setDuration(300);
            animY.start();

            headerTextContainer.setScaleX(1.1f);
            headerTextContainer.setScaleY(1.1f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(headerTextContainer, "scaleX", 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(headerTextContainer, "scaleY", 1f);

            scaleX.setDuration(300);
            scaleY.setDuration(300);

            scaleX.start();
            scaleY.start();

            headerPlus.setAlpha(1f);
            headerMinus.setAlpha(1f);

            ObjectAnimator alphaP = ObjectAnimator.ofFloat(headerPlus, "alpha", 0f);
            ObjectAnimator alphaM = ObjectAnimator.ofFloat(headerMinus, "alpha", 0f);

            alphaP.setDuration(300);
            alphaM.setDuration(300);

            alphaP.start();
            alphaM.start();

			alphaM.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					headerArrow.setLayerType(View.LAYER_TYPE_NONE, null);
					headerTextContainer.setLayerType(View.LAYER_TYPE_NONE, null);
					headerPlus.setLayerType(View.LAYER_TYPE_NONE, null);
					headerMinus.setLayerType(View.LAYER_TYPE_NONE, null);
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});

            inHeaderDetailScreen = false;
        } else {

			headerArrow.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerTextContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerPlus.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerMinus.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            headerArrow.setRotation(0f);

            ObjectAnimator rotation = ObjectAnimator.ofFloat(headerArrow,
                    "rotation", 180f);
            rotation.setDuration(300);
            rotation.start();

            headerTextContainer.setTranslationY(Resource.getPx(58, getActivity().getResources()));

            ObjectAnimator animY = ObjectAnimator.ofFloat(headerTextContainer, "translationY", 0);
            animY.setDuration(300);
            animY.start();

            headerTextContainer.setScaleX(1f);
            headerTextContainer.setScaleY(1f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(headerTextContainer, "scaleX", 1.1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(headerTextContainer, "scaleY", 1.1f);

            scaleX.setDuration(300);
            scaleY.setDuration(300);

            scaleX.start();
            scaleY.start();

            headerPlus.setAlpha(0f);
            headerMinus.setAlpha(0f);

            ObjectAnimator alphaP = ObjectAnimator.ofFloat(headerPlus, "alpha", 1f);
            ObjectAnimator alphaM = ObjectAnimator.ofFloat(headerMinus, "alpha", 1f);

            alphaP.setDuration(300);
            alphaM.setDuration(300);

            alphaP.start();
            alphaM.start();

			alphaM.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					headerArrow.setLayerType(View.LAYER_TYPE_NONE, null);
					headerTextContainer.setLayerType(View.LAYER_TYPE_NONE, null);
					headerPlus.setLayerType(View.LAYER_TYPE_NONE, null);
					headerMinus.setLayerType(View.LAYER_TYPE_NONE, null);
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});

            inHeaderDetailScreen = true;
        }
    }

	public static void updateBalance() {
		headerPlus.setText("+ " + Resource.calculateTotalPlus() + Resource.getCurrency());
		headerMinus.setText(" " + Resource.calculateTotalMinus() + Resource.getCurrency());
	}

	private void updateName() {
        //TODO get user name here
        headerName.setText("Simon Halvdansson");
	}

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

	public void setSelectedPerson(Person p) {
		adapter.selectPerson(p);
		adapter.notifyDataSetChanged();
	}

    private void selectItem(int position) {
		position -= mDrawerListView.getHeaderViewsCount();
        mCurrentSelectedPosition = position;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(adapter.getItem(position));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        mCallbacks.onShowGlobalContextActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(NavigationDrawerItem item);

        void onShowGlobalContextActionBar();
    }
}
