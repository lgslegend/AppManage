package com.lgs.AppManage.AppManage.installFragment.Backup;

import android.content.SharedPreferences;
import android.util.SparseIntArray;

import com.lgs.AppManage.R;

public class Sorter
{
    AppInfoAdapter adapter;
    FilteringMethod filteringMethod = FilteringMethod.ALL;
    SortingMethod sortingMethod = SortingMethod.PACKAGENAME;
    // SparseIntArray is more memory efficient than mapping integers to integers using a hashmap
    private static final SparseIntArray convertFilteringIdMap, convertSortingIdMap;
    SharedPreferences.Editor prefsEdit;
    int oldBackups = 0;
    public Sorter(AppInfoAdapter adapter, SharedPreferences prefs)
    {
        this.adapter = adapter;
        this.prefsEdit = prefs.edit();
        try
        {
            oldBackups = Integer.valueOf(prefs.getString("oldBackups", "0"));
        }
        catch(NumberFormatException e)
        {}
    }
    static
    {
        convertFilteringIdMap = new SparseIntArray(3);
        convertFilteringIdMap.put(FilteringMethod.ALL.ordinal(), R.id.operate_share);
        convertFilteringIdMap.put(FilteringMethod.SYSTEM.ordinal(), R.id.operate_share);
        convertFilteringIdMap.put(FilteringMethod.USER.ordinal(), R.id.operate_share);
    }
    static
    {
        convertSortingIdMap = new SparseIntArray(2);
        convertSortingIdMap.put(SortingMethod.LABEL.ordinal(), R.id.operate_share);
        convertSortingIdMap.put(SortingMethod.PACKAGENAME.ordinal(), R.id.operate_share);
    }
    public enum FilteringMethod
    {
        ALL(R.id.operate_share),
        SYSTEM(R.id.operate_share),
        USER(R.id.operate_share),
        NOTBACKEDUP(R.id.operate_share),
        NOTINSTALLED(R.id.operate_share),
        NEWANDUPDATED(R.id.operate_share),
        OLDBACKUPS(R.id.operate_share),
        ONLYAPK(R.id.operate_share),
        ONLYDATA(R.id.operate_share),
        ONLYSPECIAL(R.id.operate_share);
        int id;
        FilteringMethod(int id)
        {
            this.id = id;
        }
        int getId()
        {
            return id;
        }
    }
    public enum SortingMethod
    {
         LABEL(R.id.operate_share),
       PACKAGENAME(R.id.operate_share);
        int id;
        SortingMethod(int id)
        {
            this.id = id;
        }
        int getId()
        {
            return id;
        }
    }
   public void sort(int id)
    { /*
        switch(id)
        {
           case R.id.showAll:
                filterShowAll();
                saveInPrefs("filteringId", filteringMethod.ordinal());
                break;
            case R.id.showOnlySystem:
                filteringMethod = FilteringMethod.SYSTEM;
                adapter.filterAppType(2);
                saveInPrefs("filteringId", filteringMethod.ordinal());
                break;
            case R.id.showOnlyUser:
                filteringMethod = FilteringMethod.USER;
                adapter.filterAppType(1);
                saveInPrefs("filteringId", filteringMethod.ordinal());
                break;
            case R.id.showNotBackedup:
                filteringMethod = FilteringMethod.NOTBACKEDUP;
                adapter.filterIsBackedup();
                break;
            case R.id.showNotInstalled:
                filteringMethod = FilteringMethod.NOTINSTALLED;
                adapter.filterIsInstalled();
                break;
            case R.id.showNewAndUpdated:
                filteringMethod = FilteringMethod.NEWANDUPDATED;
                adapter.filterNewAndUpdated();
                break;
            case R.id.showOldBackups:
                filteringMethod = FilteringMethod.OLDBACKUPS;
                adapter.filterOldApps(oldBackups);
                break;
            case R.id.showOnlyApkBackedUp:
                filteringMethod = FilteringMethod.ONLYAPK;
                adapter.filterPartialBackups(AppInfo.MODE_APK);
                break;
            case R.id.showOnlyDataBackedUp:
                filteringMethod = FilteringMethod.ONLYDATA;
                adapter.filterPartialBackups(AppInfo.MODE_DATA);
                break;
            case R.id.showOnlySpecialBackups:
                filteringMethod = FilteringMethod.ONLYSPECIAL;
                adapter.filterSpecialBackups();
                break;
            case R.id.sortByLabel:
                sortingMethod = SortingMethod.LABEL;
                adapter.sortByLabel();
                sort(filteringMethod.getId());
                saveInPrefs("sortingId", sortingMethod.ordinal());
                break;
            case R.id.sortByPackageName:
                sortingMethod = SortingMethod.PACKAGENAME;
                adapter.sortByPackageName();
                sort(filteringMethod.getId());
                saveInPrefs("sortingId", sortingMethod.ordinal());
                break;
        } */
    }
    public void filterShowAll()
    {
        filteringMethod = FilteringMethod.ALL;
        adapter.filterAppType(0);
    }
    public FilteringMethod getFilteringMethod()
    {
        if(filteringMethod != null)
        {
            return filteringMethod;
        }
        return FilteringMethod.ALL;
    }
    public SortingMethod getSortingMethod()
    {
        if(sortingMethod != null)
        {
            return sortingMethod;
        }
        return SortingMethod.PACKAGENAME;
    }
    public void saveInPrefs(String prefName, int persistentId)
    {
        prefsEdit.putInt(prefName, persistentId);
        prefsEdit.commit();
    }
    // needs to be static as it is used in onPrepareOptionsMenu which is called before the sorter instance is created
    public static int convertFilteringId(int key)
    {
        // SparseIntArray returns 0 if the key is not found
        return convertFilteringIdMap.get(key);
    }
    public static int convertSortingId(int key)
    {
        return convertSortingIdMap.get(key);
    }
}