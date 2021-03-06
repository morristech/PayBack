package com.johnsimon.payback.adapter;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.core.NavigationDrawerItem;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.R;
import com.johnsimon.payback.ui.fragment.NavigationDrawerFragment;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends BaseAdapter {
	private NavigationDrawerItem allItem = new NavigationDrawerItem(NavigationDrawerItem.Type.All);
	public ArrayList<NavigationDrawerItem> items = new ArrayList<>();

	private final DataActivity activity;

	public NavigationDrawerAdapter(DataActivity _activity) {
		this.activity = _activity;
	}

	public void clearItems() {
		items.clear();
	}

	public void setItems(ArrayList<Person> people) {
		clearItems();
		for(Person person : people) {
			items.add(new NavigationDrawerItem(person.toString(), person.id, null, person));
		}
	}

	public int selectPerson(Person person) {
		if(person == null) {
			return 0;
		}

		for (int i = 0, l = items.size(); i < l; i++) {
			NavigationDrawerItem item = items.get(i);
			if(item.personId == person.id) {
				return i + 1;
			}
		}
        return 0;
	}

	@Override
	//One for allItem
	public int getCount() {
		return items.size() + 1;
	}

	@Override
	public NavigationDrawerItem getItem(int i) {
		return i == 0
				? allItem
				: items.get(i - 1);
	}

	@Override
	public long getItemId(int i) {
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder holder;
		boolean isSelected = (position == NavigationDrawerFragment.mCurrentSelectedPosition);

		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.navigation_drawer_list_item, null);

			holder = new ViewHolder(
				(TextView) convertView.findViewById(R.id.navigation_drawer_list_item_text),
				(RoundedImageView) convertView.findViewById(R.id.navigation_drawer_list_item_avatar),
				(TextView) convertView.findViewById(R.id.navigation_drawer_list_item_avatar_letter),
				convertView.findViewById(R.id.navigation_drawer_list_item_background));

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position == 0) {
			holder.title.setText(R.string.all);
			holder.avatar.setImageResource(R.drawable.ic_people_placeholder);
			holder.avatarLetter.setVisibility(View.GONE);
		} else {
			NavigationDrawerItem item = items.get(--position);
			Person owner = item.owner;

			Resource.createProfileImage(activity, owner, holder.avatar, holder.avatarLetter);

			holder.title.setText(item.title);
		}

		if (isSelected) {
			holder.title.setTypeface(null, Typeface.BOLD);
            holder.title.setTextColor(activity.getResources().getColor(R.color.green));
            holder.itemView.setBackgroundColor(activity.getResources().getColor(R.color.gray_oncolor_very_light));
		} else {
			holder.title.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(activity.getResources().getColor(R.color.gray_text_light));
            holder.itemView.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
		}

		return convertView;
	}

	static class ViewHolder {
		public TextView title;
        public RoundedImageView avatar;
		public TextView avatarLetter;
        public View itemView;

		ViewHolder(TextView title, RoundedImageView avatar, TextView avatarLetter, View itemView) {
			this.title = title;
            this.avatar = avatar;
			this.avatarLetter = avatarLetter;
            this.itemView = itemView;
		}
	}
}
