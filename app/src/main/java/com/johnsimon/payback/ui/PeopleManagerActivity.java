package com.johnsimon.payback.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.adapter.PeopleListAdapter;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.PeopleOrder;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.Undo;
import com.johnsimon.payback.view.DragSortRecycler;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.shamanland.fab.FloatingActionButton;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

public class PeopleManagerActivity extends DataActivity {

	private static String ARG_PREFIX = Resource.prefix("CREATE_DEBT");

	//TODO behålla ordning till drive

	private PeopleListAdapter adapter;
    private RecyclerView recyclerView;

    private int sortAzX;
    private int sortAzY;

    private ArrayList<Person> personListBeforeSort;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Resource.isLOrAbove()) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher), getResources().getColor(R.color.primary_color)));

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color));
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.primary_color_darker));
        }

        setContentView(R.layout.activity_people_manager);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

		recyclerView = (RecyclerView) findViewById(R.id.people_recycler_view);

		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager( new LinearLayoutManager(this));
		recyclerView.setItemAnimator(null);

		DragSortRecycler dragSortRecycler = new DragSortRecycler();
		dragSortRecycler.setViewHandleId(R.id.people_list_item_handle);
		dragSortRecycler.setFloatingAlpha(0.4f);
		dragSortRecycler.setFloatingBgColor(getResources().getColor(android.R.color.transparent));
		dragSortRecycler.setAutoScrollSpeed(0.3f);
		dragSortRecycler.setAutoScrollWindow(0.1f);

		dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
			@Override
			public void onItemMoved(int from, int to) {
				if (from != to) {
					Person item = adapter.getItem(from);
					adapter.remove(item);
					adapter.insert(item, to);
					adapter.notifyDataSetChanged();
				}
			}
		});

		recyclerView.addItemDecoration(dragSortRecycler);
		recyclerView.addOnItemTouchListener(dragSortRecycler);
		recyclerView.setOnScrollListener(dragSortRecycler.getScrollListener());

        final ImageView people_manager_empty_image = (ImageView) findViewById(R.id.people_manager_empty_image);
		people_manager_empty_image.setBackgroundResource(R.anim.hand_wave);
		people_manager_empty_image.post(new Runnable() {
            @Override
            public void run() {
                AnimationDrawable frameAnimation = (AnimationDrawable) people_manager_empty_image.getBackground();
                frameAnimation.start();
            }
        });

		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {

			int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

			if (Resource.isLOrAbove()) {
				ImageButton fab = (ImageButton) findViewById(R.id.feed_fab_l);

				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
				params.setMargins(0, actionBarHeight + Resource.getPx(48, getResources()) - Math.round(getResources().getDimension(R.dimen.fab_size) / 2), Math.round(getResources().getDimension(R.dimen.fab_right_margin)), 0);

				fab.setLayoutParams(params);

				fab.setOnClickListener(fabClickListener);
			} else {
				FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.feed_fab);

				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
				params.setMargins(0, actionBarHeight + Resource.getPx(48, getResources()) - (Resource.getPx(56, getResources()) / 2), Math.round(getResources().getDimension(R.dimen.fab_right_margin)), 0);

				fab.setLayoutParams(params);

				fab.setOnClickListener(fabClickListener);
			}
		}

		setupTreeObserver();

    }

	@Override
	protected void onDataReceived() {
		adapter = new PeopleListAdapter(this, findViewById(R.id.people_manager_empty), data, (TextView) findViewById(R.id.people_manager_title), data.peopleOrdered());
		recyclerView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		adapter.updateEmptyViewVisibility();

		final PeopleManagerActivity self = this;

		adapter.clickListener = new PeopleListAdapter.PeopleListClickListener() {
			@Override
			public void onListItemClick(int position) {
				Person person = adapter.getItem(position);
				PeopleDetailDialogFragment peopleDetailDialogFragment = PeopleDetailDialogFragment.newInstance(person);
				peopleDetailDialogFragment.show(getFragmentManager(), "people_detail_dialog");
				peopleDetailDialogFragment.editPersonCallback = new PeopleDetailDialogFragment.EditPersonCallback() {

					@Override
					public void onEdit() {
						adapter.notifyDataSetChanged();
						adapter.updateEmptyViewVisibility();
					}
				};
			}
		};
	}

	@Override
	protected void onDataLinked() {
		adapter.notifyDataSetChanged();
	}

	private View.OnClickListener fabClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			PersonPickerDialogFragment fragment = new PersonPickerDialogFragment();
			Bundle args = new Bundle();
			args.putString(PersonPickerDialogFragment.TITLE_KEY, getString(R.string.add_person));
			args.putBoolean(PersonPickerDialogFragment.NO_EXISTING_PEOPLE_FLAG, true);
			fragment.setArguments(args);

			fragment.completeCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
				@Override
				public void onSelected(String name) {
					Person person = new Person(name, ColorPalette.getInstance(PeopleManagerActivity.this));
					data.add(person);
					storage.commit();
					adapter.notifyDataSetChanged();
					adapter.updateEmptyViewVisibility();
				}
			};

			fragment.show(getFragmentManager(), "person_picker");
		}
	};

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        getMenuInflater().inflate(R.menu.people_manager, menu);
        return true;
    }

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

        final PeopleManagerActivity self = this;

        switch (item.getItemId()) {
			case android.R.id.home:
				returnToFeed();
				break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

			case R.id.action_sort_az:

				final ArrayList<Person> list = adapter.people;

				final PeopleOrder.SortResult result = data.peopleOrder.sortAlphabetically(data.people);

				sort(this, result, list);

				if (Resource.isLOrAbove() && (sortAzX == 0 && sortAzY == 0)) {
					int initialRadius = recyclerView.getWidth();

					Animator anim = ViewAnimationUtils.createCircularReveal(recyclerView, sortAzX, sortAzY, initialRadius, 0);

					anim.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);

							adapter.notifyDataSetChanged();
							adapter.updateEmptyViewVisibility();

							int finalRadius = Math.max(recyclerView.getWidth(), recyclerView.getHeight());

							Animator anim = ViewAnimationUtils.createCircularReveal(recyclerView, sortAzX, sortAzY, 0, finalRadius);

							recyclerView.setVisibility(View.VISIBLE);
							anim.setDuration(300);

							anim.addListener(new Animator.AnimatorListener() {
								@Override
								public void onAnimationStart(Animator animation) {
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									sort(self, result, list);
								}

								@Override
								public void onAnimationCancel(Animator animation) {
								}

								@Override
								public void onAnimationRepeat(Animator animation) {
								}
							});

							anim.start();

						}
					});

					anim.setDuration(300);
					anim.start();
                }

				break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void sort(Activity self, final PeopleOrder.SortResult result, final ArrayList<Person> list) {
		Undo.executeAction(self, R.string.sort_list, new Undo.UndoableAction() {
			@Override
			public void onDisplay() {
				adapter.people = result.people;
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onRevert() {
				adapter.people = list;
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onCommit() {
				data.peopleOrder = result.order;
				storage.commit();
			}
		});
	}

	@Override
	public void onBackPressed() {
		returnToFeed();
	}

    private void setupTreeObserver() {
        final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				View menuButton = findViewById(R.id.action_sort_az);
				if (menuButton != null) {
					int[] location = new int[2];
					menuButton.getLocationInWindow(location);

					sortAzX = location[0];
					sortAzY = location[1];

					if (viewTreeObserver.isAlive()) {
						viewTreeObserver.removeGlobalOnLayoutListener(this);
					}

				}
			}
		});
    }

	public void returnToFeed() {
		Intent intent = new Intent(this, FeedActivity.class);
		if(!data.people.contains(FeedActivity.person)) {
			FeedActivity.person = null;
		}

        storage.commit();

		finishAffinity();
		startActivity(intent);
	}
}