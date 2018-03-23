package zielu.gittoolbox.ui;

import com.google.common.collect.ImmutableMap;
import java.util.OptionalInt;
import jodd.util.StringBand;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.status.BehindStatus;

public enum StatusPresenters implements StatusPresenter {
  arrows {
    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = new StringBand(behind.behind()).append(UtfSeq.ARROW_DOWN);
      behind.delta().ifPresent(delta -> text.append(" ").append(formatDelta(delta, UtfSeq.INCREMENT)));
      return text.toString();
    }

    @Override
    public String aheadBehindStatus(int ahead, int behind) {
      return ahead + UtfSeq.ARROW_UP + " " + behind + UtfSeq.ARROW_DOWN;
    }

    @Override
    public String nonZeroAheadBehindStatus(int ahead, int behind) {
      String aheadText = "";
      if (ahead > 0) {
        aheadText = ahead + UtfSeq.ARROW_UP;
      }
      String behindText = "";
      if (behind > 0) {
        behindText = behind + UtfSeq.ARROW_DOWN;
      }
      return format(aheadText, behindText);
    }

    @Override
    public String key() {
      return "arrows";
    }

    @Override
    public String getLabel() {
      return ResBundle.getString("presentation.label.arrows");
    }
  },
  arrowHeads {
    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = new StringBand(behind.behind()).append(UtfSeq.ARROWHEAD_DOWN);
      behind.delta().ifPresent(delta -> text.append(" ").append(formatDelta(delta)));
      return text.toString();
    }

    @Override
    public String aheadBehindStatus(int ahead, int behind) {
      return ahead + UtfSeq.ARROWHEAD_UP + " " + behind + UtfSeq.ARROWHEAD_DOWN;
    }

    @Override
    public String nonZeroAheadBehindStatus(int ahead, int behind) {
      String aheadText = "";
      if (ahead > 0) {
        aheadText = ahead + UtfSeq.ARROWHEAD_UP;
      }
      String behindText = "";
      if (behind > 0) {
        behindText = behind + UtfSeq.ARROWHEAD_DOWN;
      }
      return format(aheadText, behindText);
    }

    @Override
    public String key() {
      return "arrowHeads";
    }

    @Override
    public String getLabel() {
      return ResBundle.getString("presentation.label.arrowHeads");
    }
  },
  text {
    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = new StringBand(behind.behind()).append(" ").append(ResBundle.getString("git.behind"));
      behind.delta().ifPresent(delta -> text.append(" ").append(formatDelta(delta)));
      return text.toString();
    }

    @Override
    public String aheadBehindStatus(int ahead, int behind) {
      return ahead + " // " + behind;
    }

    @Override
    public String nonZeroAheadBehindStatus(int ahead, int behind) {
      if (ahead > 0 && behind > 0) {
        return aheadBehindStatus(ahead, behind);
      } else if (ahead > 0) {
        return ahead + " " + ResBundle.getString("git.ahead");
      } else if (behind > 0) {
        return behind + " " + ResBundle.getString("git.behind");
      } else {
        return "";
      }
    }

    @Override
    public String key() {
      return "text";
    }

    @Override
    public String getLabel() {
      return ResBundle.getString("presentation.label.text");
    }
  };

  private static final ImmutableMap<String, StatusPresenter> presenters;

  static {
    ImmutableMap.Builder<String, StatusPresenter> builder = ImmutableMap.builder();
    for (StatusPresenters presenter : values()) {
      builder.put(presenter.key(), presenter);
    }
    presenters = builder.build();
  }

  public static StatusPresenter forKey(String key) {
    return presenters.get(key);
  }

  private static String format(String aheadText, String behindText) {
    if (aheadText.length() > 0 && behindText.length() > 0) {
      return aheadText + " " + behindText;
    } else if (aheadText.length() == 0 && behindText.length() == 0) {
      return "";
    } else {
      if (aheadText.length() > 0) {
        return aheadText;
      } else {
        return behindText;
      }
    }
  }

  private static String formatDelta(int delta) {
    if (delta > 0) {
      return "+ " + delta;
    } else if (delta < 0) {
      return "- " + Math.abs(delta);
    }
    return "";
  }

  private static String formatDelta(int delta, String symbol) {
    if (delta > 0) {
      return symbol + " " + delta;
    }
    return "";
  }
}
