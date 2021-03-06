package eu.mysmartline.appv3;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayAdapter extends BaseAdapter {
	private ArrayList<DisplayModel> arrayList;
	Context context;

	public DisplayAdapter(ArrayList<DisplayModel> data, Context context) {
		this.arrayList = data;
		this.context = context;
	}

	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater li = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = li.inflate(R.layout.activity_activity7_list_item, null);
		}
		
		ImageView image = (ImageView)view.findViewById(R.id.icon);
		TextView message = (TextView)view.findViewById(R.id.message);
		TextView time = (TextView)view.findViewById(R.id.time);
		
		DisplayModel displayModel = arrayList.get(position);
		
		image.setImageResource(displayModel.icon);
		message.setText(displayModel.message);
		time.setText(displayModel.time);
		
		return view;
	}

}
