package com.konka.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class Allapp extends Activity implements OnItemClickListener
{
	private InstalledReceiver installedReceiver;
	
	private GridView mGridView;
	private Context mContext;
	private PackageManager mPackageManager;
	private List<ResolveInfo> mAllApps;
	
	private final String TAG = "allapp";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i("lifeLog", "Allapp onCreate");
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//ȫ��
		setContentView(R.layout.allapps);
		
		setupViews();
	}
	
	@Override
	protected void onResume()
	{
		Log.i("lifeLog", "Allapp onResume");
		
		installedReceiver = new InstalledReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		
		this.registerReceiver(installedReceiver, filter);
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		Log.i("lifeLog", "Allapp onDestroy");
		
		unregisterReceiver(installedReceiver);
		
		super.onDestroy();
	}
	
	public void setupViews()
	{
		mContext = Allapp.this;
		mPackageManager = getPackageManager();
		mGridView = (GridView) findViewById(R.id.allapps);
		bindAllApps();

		mGridView.setAdapter(new GridItemAdapter(mContext, mAllApps));
		mGridView.setNumColumns(5);
		mGridView.setOnItemClickListener(this);
	}
	
	public void bindAllApps()
	{
		
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		
		mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);
		Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(mPackageManager));
		
		doFilter();
//		sort();
	}
	
	//过滤
	public void doFilter()
	{
		for(int i=0; i<mAllApps.size(); i++)
		{
			ResolveInfo res = mAllApps.get(i);
			String pkg = res.activityInfo.packageName;
			if(pkg.equals("com.iptv.bjunicom"))
			{
				mAllApps.remove(i);
				i--;
			}
		}
	}
	
	//排序
	public void sort()
	{
		int length = mAllApps.size();
		List systemApp = new LinkedList();
		List userApp = new LinkedList();
		
		for(int i=0; i<mAllApps.size(); i++)
		{
			ResolveInfo res = mAllApps.get(i);
			
			if(((res.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0) &&
					((res.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0))
			{
				userApp.add(mAllApps.get(i));
			}
			else
			{
				systemApp.add(mAllApps.get(i));
			}
		}
		
		mAllApps = new LinkedList();
		mAllApps.addAll(systemApp);
		mAllApps.addAll(userApp);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		ResolveInfo res = mAllApps.get(position);
		
		String pkg = res.activityInfo.packageName;
		String cls = res.activityInfo.name;
		
		Log.i(TAG, pkg + "   " + cls);
		
		ComponentName component = new ComponentName(pkg, cls);
		Intent intent = new Intent();
		intent.setComponent(component);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP 
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	private class GridItemAdapter extends BaseAdapter
	{
		private Context context;
		private List<ResolveInfo> resInfo;

		public GridItemAdapter(Context c, List<ResolveInfo> res)
		{
			context = c;
			resInfo = res;
		}

		@Override
		public int getCount()
		{
			return resInfo.size();
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.application_layout, null);

			ImageView app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
			TextView app_tilte = (TextView) convertView.findViewById(R.id.app_title);

			ResolveInfo res = resInfo.get(position);
			
			app_icon.setImageDrawable(res.loadIcon(mPackageManager));
			app_tilte.setText(res.loadLabel(mPackageManager).toString());
			
			return convertView;
		}
	}
	
	class InstalledReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Allapp.this.setupViews();
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		Log.i(TAG, "" + event.getKeyCode());
		
		return super.dispatchKeyEvent(event);
	}
}
