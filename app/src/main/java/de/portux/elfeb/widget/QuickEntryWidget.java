package de.portux.elfeb.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
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
      Intent simpleEntry = new Intent(context, EntryActivity.class);
      PendingIntent simpleEntryPendingIntent = PendingIntent.getActivity(context, RQ_NEW_ENTRY, simpleEntry, PendingIntent.FLAG_UPDATE_CURRENT);
      Intent entryWithImage = new Intent(context, EntryActivity.class);
      entryWithImage.putExtra(EntryActivity.ATTACH_IMMEDIATELY, EntryActivity.ATTACH_IMAGE);
      PendingIntent entryWithImagePendingIntent = PendingIntent.getActivity(context, RQ_IMAGE_ENTRY, entryWithImage, PendingIntent.FLAG_UPDATE_CURRENT);
      Intent entryWithAudio = new Intent(context, EntryActivity.class);
      entryWithAudio.putExtra(EntryActivity.ATTACH_IMMEDIATELY, EntryActivity.ATTACH_AUDIO);
      PendingIntent entryWithAudioPendingIntent = PendingIntent.getActivity(context, RQ_AUDIO_ENTRY, entryWithAudio, PendingIntent.FLAG_UPDATE_CURRENT);

      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quick_entry);
      remoteViews.setOnClickPendingIntent(R.id.widget_add_entry, simpleEntryPendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.widget_add_photo, entryWithImagePendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.widget_add_audio, entryWithAudioPendingIntent);

      remoteViews.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher);
      remoteViews.setImageViewResource(R.id.widget_add_entry, R.drawable.ic_add);
      remoteViews.setImageViewResource(R.id.widget_add_photo, R.drawable.ic_add_photo);
      remoteViews.setImageViewResource(R.id.widget_add_audio, R.drawable.ic_add_audio);

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
}

