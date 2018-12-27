package de.portux.elfeb.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import de.portux.elfeb.R;
import de.portux.elfeb.ui.EntryActivity;

/**
 * Implementation of App Widget functionality.
 */
public class QuickEntryWidget extends AppWidgetProvider {

  private static final int RQ_NEW_ENTRY = 111;
  private static final int RQ_IMAGE_ENTRY = 222;
  private static final int RQ_AUDIO_ENTRY = 333;

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // There may be multiple widgets active, so update all of them
    for (int appWidgetId : appWidgetIds) {
      Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
      int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
      RemoteViews remoteViews = getRemoteViews(context, determineNumberOfCells(minWidth));
      appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
  }

  @Override
  public void onEnabled(Context context) {
    // Enter relevant functionality for when the first widget is created
  }

  @Override
  public void onDisabled(Context context) {
    // Enter relevant functionality for when the last widget is disabled
  }

  @Override
  public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
      int appWidgetId, Bundle newOptions) {
    Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
    int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
    appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, determineNumberOfCells(minWidth)));
    super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
  }

  private RemoteViews getRemoteViews(Context context, int rows) {
    RemoteViews remoteViews;
    switch (rows) {
      case 4:   // audio + image + default + icon
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quick_entry_full);
        break;
      case 3:   // image + default + icon
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quick_entry_default_and_image);
        break;
      case 2:   // default + icon
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quick_entry_default_only);
        break;
      case 1:   // icon only
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quick_entry_minimal);
        break;
      default:  // more than 4 rows => everything
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quick_entry_full);
    }

    Intent entryWithAudio = new Intent(context, EntryActivity.class);
    entryWithAudio.putExtra(EntryActivity.ATTACH_IMMEDIATELY, EntryActivity.ATTACH_AUDIO);
    PendingIntent entryWithAudioPendingIntent = PendingIntent.getActivity(context, RQ_AUDIO_ENTRY, entryWithAudio, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent entryWithImage = new Intent(context, EntryActivity.class);
    entryWithImage.putExtra(EntryActivity.ATTACH_IMMEDIATELY, EntryActivity.ATTACH_IMAGE);
    PendingIntent entryWithImagePendingIntent = PendingIntent.getActivity(context, RQ_IMAGE_ENTRY, entryWithImage, PendingIntent.FLAG_UPDATE_CURRENT);
    Intent simpleEntry = new Intent(context, EntryActivity.class);
    PendingIntent simpleEntryPendingIntent = PendingIntent.getActivity(context, RQ_NEW_ENTRY, simpleEntry, PendingIntent.FLAG_UPDATE_CURRENT);

    switch (rows) {
      case 4:   // audio + image + default + icon
        entryWithAudio.putExtra(EntryActivity.ATTACH_IMMEDIATELY, EntryActivity.ATTACH_AUDIO);
        remoteViews.setOnClickPendingIntent(R.id.widget_add_audio, entryWithAudioPendingIntent);
        remoteViews.setImageViewResource(R.id.widget_add_audio, R.drawable.ic_mic_white_48dp);
        // fall through
      case 3:   // image + default + icon
        entryWithImage.putExtra(EntryActivity.ATTACH_IMMEDIATELY, EntryActivity.ATTACH_IMAGE);
        remoteViews.setOnClickPendingIntent(R.id.widget_add_photo, entryWithImagePendingIntent);
        remoteViews.setImageViewResource(R.id.widget_add_photo, R.drawable.ic_add_a_photo_white_48dp);
        // fall through
      case 2:   // default + icon
        remoteViews.setOnClickPendingIntent(R.id.widget_add_entry, simpleEntryPendingIntent);
        remoteViews.setImageViewResource(R.id.widget_add_photo, R.drawable.ic_add_a_photo_white_48dp);
        // fall through
      case 1:   // icon only
        remoteViews.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher);
        break;
      default:  // more than 4 rows => everything
        remoteViews.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher);
        remoteViews.setOnClickPendingIntent(R.id.widget_add_entry, simpleEntryPendingIntent);
        remoteViews.setImageViewResource(R.id.widget_add_photo, R.drawable.ic_add_a_photo_white_48dp);
        remoteViews.setOnClickPendingIntent(R.id.widget_add_photo, entryWithImagePendingIntent);
        remoteViews.setImageViewResource(R.id.widget_add_photo, R.drawable.ic_add_a_photo_white_48dp);
        remoteViews.setOnClickPendingIntent(R.id.widget_add_audio, entryWithAudioPendingIntent);
        remoteViews.setImageViewResource(R.id.widget_add_audio, R.drawable.ic_mic_white_48dp);
    }
    return remoteViews;
  }


  private int determineNumberOfCells(int size) {
    // see : https://developer.android.com/guide/practices/ui_guidelines/widget_design#anatomy_determining_size
    return (size + 30) / 70;
  }

}

