package zielu.gittoolbox.ui;

import com.google.common.collect.ImmutableMap;
import jodd.util.StringBand;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.status.BehindStatus;

public enum StatusPresenters implements StatusPresenter {
  arrows {
    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = formatBehind(behind, UtfSeq.ARROW_DOWN);
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
    public String branchAndParent(String branchName, String parentBranchName) {
      return branchName + " " + UtfSeq.ARROW_RIGHT + " " + parentBranchName;
    }

    @Override
    public String key() {
      return "arrows";
    }

    @Override
    public String getLabel() {
      return ResBundle.message("presentation.label.arrows");
    }
  },
  arrowHeads {
    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = formatBehind(behind, UtfSeq.ARROWHEAD_DOWN);
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
    public String branchAndParent(String branchName, String parentBranchName) {
      return branchName + " " + UtfSeq.ARROWHEAD_RIGHT + " " + parentBranchName;
    }

    @Override
    public String key() {
      return "arrowHeads";
    }

    @Override
    public String getLabel() {
      return ResBundle.message("presentation.label.arrowHeads");
    }
  },
  text {
    private final String behindSymbol = " " + ResBundle.message("git.behind");
    private final String aheadSymbol = " " + ResBundle.message("git.ahead");

    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = formatBehind(behind, behindSymbol);
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
        return ahead + aheadSymbol;
      } else if (behind > 0) {
        return behind + behindSymbol;
      } else {
        return "";
      }
    }

    @Override
    public String branchAndParent(String branchName, String parentBranchName) {
      return branchName + " > " + parentBranchName;
    }

    @Override
    public String key() {
      return "text";
    }

    @Override
    public String getLabel() {
      return ResBundle.message("presentation.label.text");
    }
  };

  private static final ImmutableMap<String, StatusPresenter> PRESENTERS;

  static {
    ImmutableMap.Builder<String, StatusPresenter> builder = ImmutableMap.builder();
    for (StatusPresenters presenter : values()) {
      builder.put(presenter.key(), presenter);
    }
    PRESENTERS = builder.build();
  }

  public static StatusPresenter forKey(String key) {
    return PRESENTERS.get(key);
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

  private static StringBand formatBehind(BehindStatus behind, String symbol) {
    return new StringBand().append(behind.behind()).append(symbol);
  }

  private static String formatDelta(int delta) {
    if (delta > 0) {
      return "+" + delta;
    } else if (delta < 0) {
      return String.valueOf(delta);
    }
    return "";
  }

  private static String formatDelta(int delta, String symbol) {
    if (delta != 0) {
      return symbol + delta;
    }
    return "";
  }
}
