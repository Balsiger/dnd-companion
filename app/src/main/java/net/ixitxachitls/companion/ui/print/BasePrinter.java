package net.ixitxachitls.companion.ui.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import net.ixitxachitls.companion.R;

import java.util.Optional;

/**
 * Base class for printing things.
 */
public abstract class BasePrinter<T> {

  private static final int WIDTH = 595;
  private static final int HEIGHT = 842;

  protected Context context;

  // Print styles.
  protected Paint valueStyle = new Paint();
  protected Paint labelStyle = new Paint();
  protected Paint lineStyle = new Paint();
  protected Paint titleStyle = new Paint();
  protected Paint barStyle = new Paint();
  protected Paint largeStyle = new Paint();
  protected Paint smallStyle = new Paint();
  protected Paint tinyStyle = new Paint();

  protected BasePrinter(Context context) {
    this.context = context;

    // Printing.
    labelStyle.setColor(context.getColor(R.color.grey));
    labelStyle.setTextSize(6);
    lineStyle.setStrokeWidth(1);
    lineStyle.setColor(context.getColor(R.color.grey_dark));
    titleStyle.setColor(context.getColor(R.color.white));
    titleStyle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    titleStyle.setTextSize(10);
    barStyle.setColor(context.getColor(R.color.black));
    largeStyle.setTextSize(12);
    largeStyle.setTypeface(Typeface.DEFAULT_BOLD);
    smallStyle.setTextSize(8);
    tinyStyle.setTextSize(4);
  }

  public PdfDocument print(T source) {
    PdfDocument document = new PdfDocument();

    int count = 1;
    boolean next = false;
    do {
      PdfDocument.PageInfo pageInfo =
          new PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, count).create();
      PdfDocument.Page page = document.startPage(pageInfo);
      Canvas canvas = page.getCanvas();
      next = printPage(source, canvas, count);
      document.finishPage(page);
      count++;
    } while (next);

    return document;
  }

  protected abstract boolean printPage(T source, Canvas canvas, int count);

  protected void printSmallValue(Canvas canvas, String value, String label,
                                 Optional<String> label2, int startX, int startY, int endX) {
    canvas.drawText(value, startX, startY, valueStyle);
    canvas.drawLine(startX - 2, startY + 4, endX, startY + 4, lineStyle);
    canvas.drawText(label, startX, startY + 9, tinyStyle);
    if (label2.isPresent()) {
      canvas.drawText(label2.get(), startX, startY + 14, tinyStyle);
    }
  }

  protected void printTitle(Canvas canvas, Optional<Bitmap> image, String title, int startX,
                            int startY, int endX) {
    canvas.drawRect(startX - 5, startY - 0.25f, endX, startY + 19.5f, barStyle);
    if (image.isPresent()) {
      canvas.drawBitmap(image.get(), null, new Rect(startX, startY - 5, startX + 30, startY + 25),
          null);
    }
    canvas.drawText(title, startX + (image.isPresent() ? 40 : 10), startY + 17, titleStyle);
  }

  protected void printValue(Canvas canvas, String value, String label, int startX, int startY,
                            int endX) {
    canvas.drawText(value, startX, startY, valueStyle);
    canvas.drawLine(startX - 5, startY + 5, endX, startY + 5, lineStyle);
    canvas.drawText(label, startX - 5, startY + 13, labelStyle);
  }
}
