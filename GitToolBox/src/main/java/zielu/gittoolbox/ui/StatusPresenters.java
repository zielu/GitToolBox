package zielu.gittoolbox.ui;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import jodd.util.StringBand;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.status.BehindStatus;
import zielu.gittoolbox.util.Count;

public enum StatusPresenters implements StatusPresenter {
  arrows {
    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = formatBehind(behind, UtfSeq.ARROW_DOWN);
      behind.delta()
          .ifPresent(delta -> {
            text.append(" ");
            formatDelta(text, delta, UtfSeq.INCREMENT);
          });
      return text.toString();
    }

    @Override
    public String aheadBehindStatus(int ahead, int behind) {
      return new StringBand(5)
                 .append(ahead)
                 .append(UtfSeq.ARROW_UP)
                 .append(" ")
                 .append(behind)
                 .append(UtfSeq.ARROW_DOWN)
                 .toString();
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
    public String extendedRepoInfo(ExtendedRepoInfo extendedRepoInfo) {
      return formatExtendedRepoInfo(extendedRepoInfo,
          value -> formatChanged(value, UtfSeq.EMPTY_SET, UtfSeq.DELTA));
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
    private final String noChangesSymbol = UtfSeq.ARROWHEAD_LEFT + UtfSeq.ARROWHEAD_RIGHT;

    @Override
    public String behindStatus(BehindStatus behind) {
      StringBand text = formatBehind(behind, UtfSeq.ARROWHEAD_DOWN);
      behind.delta()
          .ifPresent(delta -> text.append(" ")
                                  .append(formatDelta(delta)));
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
    public String extendedRepoInfo(ExtendedRepoInfo extendedRepoInfo) {
      return formatExtendedRepoInfo(extendedRepoInfo,
          value -> formatChanged(value, noChangesSymbol, UtfSeq.DELTA));
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
      behind.delta()
          .ifPresent(delta -> text.append(" ")
                                  .append(formatDelta(delta)));
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
    public String extendedRepoInfo(ExtendedRepoInfo extendedRepoInfo) {
      return formatExtendedRepoInfo(extendedRepoInfo,
          value -> ResBundle.message("change.count.x.changes.label", value));
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

  protected String format(String aheadText, String behindText) {
    if (aheadText.length() > 0 && behindText.length() > 0) {
      return new StringBand(3)
                 .append(aheadText)
                 .append(" ")
                 .append(behindText)
                 .toString();
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

  protected StringBand formatBehind(BehindStatus behind, String symbol) {
    return new StringBand()
               .append(behind.behind())
               .append(symbol);
  }

  protected String formatDelta(int delta) {
    if (delta > 0) {
      return new StringBand(2)
                 .append("+")
                 .append(delta)
                 .toString();
    } else if (delta < 0) {
      return String.valueOf(delta);
    }
    return "";
  }

  protected StringBand formatDelta(StringBand target, int delta, String symbol) {
    if (delta != 0) {
      return target
                 .append(symbol)
                 .append(delta);
    }
    return target;
  }

  protected String formatExtendedRepoInfo(ExtendedRepoInfo extendedRepoInfo,
                                          IntFunction<String> formatChanged) {
    List<String> parts = new ArrayList<>();
    Count changedCount = extendedRepoInfo.getChangedCount();
    if (!changedCount.isEmpty()) {
      parts.add(formatChanged.apply(changedCount.getValue()));
    }
    return String.join(" ", parts);
  }

  protected String formatChanged(int changesCount, String noChangesSymbol, String changedSymbol) {
    if (changesCount == 0) {
      return noChangesSymbol;
    } else {
      return new StringBand(3)
                 .append(changesCount)
                 .append(" ")
                 .append(changedSymbol)
                 .toString();
    }
  }
}
