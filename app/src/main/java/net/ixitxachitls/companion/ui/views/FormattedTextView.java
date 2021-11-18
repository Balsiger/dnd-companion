package net.ixitxachitls.companion.ui.views;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ixitxachitls.companion.R;
import net.ixitxachitls.companion.ui.views.wrappers.TextWrapper;
import net.ixitxachitls.companion.ui.views.wrappers.Wrapper;
import net.ixitxachitls.companion.util.Texts;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

/**
 * A text view that displays formatted companion text with evaluated expressions.
 */
public class FormattedTextView extends LinearLayout {

  // UI elements.
  private TextWrapper<TextView> text;
  private Wrapper<LinearLayout> processing;
  private TextWrapper<TextView> context;
  private TextWrapper<TextView> raw;

  public FormattedTextView(Context context) {
    this(context, null);
  }

  public FormattedTextView(Context context, @Nullable AttributeSet attributes) {
    super(context, attributes);

    init(attributes);
  }

  public FormattedTextView text(String message, Texts.Values values) {
    context.text(values.toString());
    raw.text(message);
    text.text(Texts.processCommands(getContext(), message, values));
    text.get().setMovementMethod(LinkMovementMethod.getInstance());

    return this;
  }

  @CallSuper
  protected View init(AttributeSet attributes) {
    LinearLayout view = (LinearLayout)
        LayoutInflater.from(getContext()).inflate(R.layout.view_formatted_text, null, false);
    text = TextWrapper.wrap(new TextView(getContext(), attributes));
    text.onLongClick(() -> {
      processing.toggleVisiblity();
    });
    view.addView(text.get());

    addView(view);

    processing = Wrapper.wrap(view, R.id.processing);
    processing.gone().onClick(() -> processing.gone());
    context = TextWrapper.wrap(view, R.id.context);
    raw = TextWrapper.wrap(view, R.id.raw);

    return view;
  }
}
