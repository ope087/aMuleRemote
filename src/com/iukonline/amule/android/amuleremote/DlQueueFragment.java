package com.iukonline.amule.android.amuleremote;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.DlQueueWatcher;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;
import com.iukonline.amule.ec.ECStats;

public class DlQueueFragment extends ListFragment implements DlQueueWatcher {
    
    public interface DlQueueFragmentContainer {
        public void partFileSelected(byte[] hash);
    }
    
    private final static String BUNDLE_SORT_BY = "sort";


    AmuleControllerApplication mApp;

    
    ArrayList <ECPartFile> mDlQueue;
    DownloadListAdapter mDlAdapter;
    byte mSortBy;
    ECPartFileComparator mDlQueueComparator;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mApp = (AmuleControllerApplication) getActivity().getApplication();
        
        if (savedInstanceState == null) {
            mSortBy = (byte) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_FILENAME);
            //mSortBy = AmuleControllerApplication.AC_SETTING_SORT_FILENAME;
        } else {
            mSortBy = savedInstanceState.getByte(BUNDLE_SORT_BY, AmuleControllerApplication.AC_SETTING_SORT_FILENAME);
        }
        mDlQueueComparator = new ECPartFileComparator(AmuleControllerApplication.getDlComparatorTypeFromSortSetting(mSortBy));

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            //return null;
        }
        
        View v = inflater.inflate(R.layout.dlqueue_fragment, container, false);
        
        return v;

    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateDlQueue(mApp.mECHelper.registerForDlQueueUpdates(this));
    }
    
    @Override
    public void onPause() {
        
        mApp.mECHelper.unRegisterFromDlQueueUpdates(this);
        super.onPause();
    }
    
    
    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByte(BUNDLE_SORT_BY, mSortBy);
    }
    
    
    
    
    
    
    
    
    


    public void refreshView() {
        
        ECStats stats = mApp.mECHelper.getStats();
        if (stats != null) {
            ((TextView) getView().findViewById(R.id.main_dl_rate)).setText(GUIUtils.longToBytesFormatted(stats.getSpeedDl()) + "/s");
            ((TextView) getView().findViewById(R.id.main_ul_rate)).setText(GUIUtils.longToBytesFormatted(stats.getSpeedUl()) + "/s");
        }
        
        if (mDlAdapter != null) {
            mDlAdapter.notifyDataSetChanged();
        }
        
/*        if (mDlQueue != null) {
            ECPartFileComparator c = new ECPartFileComparator((byte) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SORT, ECPartFileComparator.AC_SETTING_SORT_FILENAME));
            Collections.sort(mDlQueue, c);
            DownloadListAdapter dlListAdapter = new DownloadListAdapter(getActivity(), R.layout.dlqueue_fragment, mDlQueue );
            
            setListAdapter(dlListAdapter);
        } else {
            ((TextView) getView().findViewById(android.R.id.empty)).setText(R.string.dlqueue_server_not_queried);
        }*/
    }   
    
    
    @Override
    public void updateDlQueue(ArrayList<ECPartFile> newDlQueue) {

        boolean createAdapter = false;

        if (newDlQueue != null) {
            //setEmptyText(getString(R.string.dlqueue_empty));
            
            ((TextView) getListView().getEmptyView()).setText(R.string.dlqueue_empty);
            
            if (mDlQueue == null) {
                createAdapter = true;
            }
            
        }
        mDlQueue = newDlQueue;
        
        
        if (mDlQueue != null) {
            Collections.sort(mDlQueue, mDlQueueComparator);
            
            if (createAdapter) {
                mDlAdapter = new DownloadListAdapter(getActivity(), R.layout.dlqueue_fragment, mDlQueue );
                setListAdapter(mDlAdapter);
            }
        }

        refreshView();
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
       
        ((DlQueueFragmentContainer) getActivity()).partFileSelected(mDlQueue.get(position).getHash());
        
    }
    
    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    

    private class DownloadListAdapter extends ArrayAdapter<ECPartFile> {

        private ArrayList<ECPartFile> items;

        public DownloadListAdapter(Context context, int textViewResourceId, ArrayList<ECPartFile> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.amuledl_row, null);
                }
                ECPartFile o = items.get(position);
                if (o != null) {
                    
                        float perc = ((float) o.getSizeDone()) * 100f / ((float) o.getSizeFull());
                    
                        ((TextView) v.findViewById(R.id.amuledl_row_filename)).setText(o.getFileName());
                        ((TextView) v.findViewById(R.id.amuledl_row_transferred)).setText(String.format("%s/%s (%.1f%%)", 
                                        GUIUtils.longToBytesFormatted(o.getSizeDone()), 
                                        GUIUtils.longToBytesFormatted(o.getSizeFull()),
                                        perc
                                        ));
                        
                        v.findViewById(R.id.amuledl_row_hascomments).setVisibility(o.hasComments() ? View.VISIBLE : View.GONE);
                        
                        int sourceCount = o.getSourceCount();
                        int sourceA4AF = o.getSourceA4AF();
                        int sourceXfer = o.getSourceXfer();
                        int sourceNotCur = o.getSourceNotCurrent();
                        
                        StringBuffer source = new StringBuffer(Integer.toString(sourceCount));
                        if (sourceNotCur > 0) source.append("/" + Integer.toString(sourceCount + sourceNotCur));
                        if (sourceA4AF > 0) source.append("+"+Integer.toString(sourceA4AF));
                        if (sourceXfer > 0) source.append(" (" + Integer.toString(sourceXfer) + ")");
                        
                        ((TextView) v.findViewById(R.id.amuledl_row_sources)).setText(source.toString());
                        
                     // TODO Convert to string resources...

                        String sEta;
                        if (o.getSpeed() > 0) {
                            long eta = (o.getSizeFull() - o.getSizeDone()) / o.getSpeed();
                            if (eta < 60) {
                                sEta = Long.toString(eta) + " secs";
                            } else if (eta < 3600) {
                                sEta = String.format("%d:%02d mins", eta / 60, eta % 60);
                            } else if (eta < 86400) {
                                sEta = String.format("%d:%02d hours", eta / 3600, (eta % 3600) / 60);
                            } else {
                                sEta = Long.toString(eta / 86400) + " days";
                            }
                        } else {
                            sEta = "";
                        }
                        ((TextView) v.findViewById(R.id.amuledl_row_eta)).setText(sEta);
                        
                        ProgressBar bar = (ProgressBar) v.findViewById(R.id.amuledl_row_progress);
                        

                        
                        TextView tvStatus = (TextView) v.findViewById(R.id.amuledl_row_status);
                        TextView tvPrio = (TextView) v.findViewById(R.id.amuledl_row_prio);
                        
                        switch (o.getPrio()) {
                        case ECPartFile.PR_LOW:
                            tvPrio.setText(R.string.partfile_prio_low);
                            break;
                        case ECPartFile.PR_NORMAL:
                            tvPrio.setText(R.string.partfile_prio_normal);
                            break;
                        case ECPartFile.PR_HIGH:
                            tvPrio.setText(R.string.partfile_prio_high);
                            break;
                        case ECPartFile.PR_AUTO_LOW:
                            tvPrio.setText(R.string.partfile_prio_auto_low);
                            break;
                        case ECPartFile.PR_AUTO_NORMAL:
                            tvPrio.setText(R.string.partfile_prio_auto_normal);
                            break;
                        case ECPartFile.PR_AUTO_HIGH:
                            tvPrio.setText(R.string.partfile_prio_auto_high);
                            break;
                        default:
                            tvPrio.setText(R.string.partfile_prio_unknown);
                            break;
                        }
                        
                        int barResource = 0;
                                        
 
                        switch (o.getStatus()) {
                        
                        case ECPartFile.PS_ALLOCATING:
                            // TODO What's this?
                            tvStatus.setText(R.string.partfile_status_allocating);
                            barResource = R.drawable.file_progress_waiting;
                            break;
                        case ECPartFile.PS_COMPLETE:
                            tvStatus.setText(R.string.partfile_status_complete);
                            barResource = R.drawable.file_progress_running;
                            break;
                        case ECPartFile.PS_COMPLETING:
                            tvStatus.setText(R.string.partfile_status_completing);
                            barResource = R.drawable.file_progress_running;
                            break;
                        case ECPartFile.PS_EMPTY:
                            // TODO: Check why this happens...
                            tvStatus.setText(R.string.partfile_status_empty);
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        case ECPartFile.PS_ERROR:
                            tvStatus.setText(R.string.partfile_status_error);
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        case ECPartFile.PS_HASHING:
                            tvStatus.setText(R.string.partfile_status_hashing);
                            barResource = R.drawable.file_progress_waiting;
                            break;
                        case ECPartFile.PS_INSUFFICIENT:
                            // TODO What's this?
                            tvStatus.setText(R.string.partfile_status_insuffcient);
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        case ECPartFile.PS_PAUSED:
                            tvStatus.setText(R.string.partfile_status_paused);
                            barResource = R.drawable.file_progress_stopped;
                            break;
                        case ECPartFile.PS_READY:
                            if (sourceXfer > 0) {
                                tvStatus.setText(R.string.partfile_status_downloading);
                                tvStatus.append( " " + GUIUtils.longToBytesFormatted(o.getSpeed()) + "/s");
                                barResource = R.drawable.file_progress_running;
                            } else {
                                tvStatus.setText(R.string.partfile_status_waiting);
                                barResource = R.drawable.file_progress_waiting;
                            } 
                            break;
                        case ECPartFile.PS_UNKNOWN:
                            tvStatus.setText(R.string.partfile_status_unknown);
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        case ECPartFile.PS_WAITINGFORHASH:
                            // TODO What's this?
                            tvStatus.setText(R.string.partfile_status_waitingforhash);
                            barResource = R.drawable.file_progress_waiting;
                            break;
                            
                        default:
                            tvStatus.setText("UNKNOWN-" + o.getStatus());
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        }
                        
                        
                        // Note: get/set Bounds is needed. Otherwise the bar would disappear on scroll. WA found on
                        // http://stackoverflow.com/questions/2805866/android-progressbar-setprogressdrawable-only-works-once

                        Rect bounds = bar.getProgressDrawable().getBounds();
                        bar.setProgressDrawable(getResources().getDrawable(barResource));
                        bar.getProgressDrawable().setBounds(bounds);
                        bar.setProgress((int) (bar.getMax() * perc / 100));
                 
                }
                return v;
        }
    }


    

    
}