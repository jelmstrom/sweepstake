package com.jelmstrom.tips;


import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import static com.jelmstrom.tips.match.Match.Stage.valueOf;
import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping(value = "/playoff")
public class PlayoffController extends BaseController {

    public PlayoffController() {
        super();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String playoff(Model uiModel, HttpServletRequest request) {
        User sessionUser = setSessionUsers(request, uiModel);

        SortedMap<Match.Stage, List<Match>> playoffMap = matchRepository.getPlayoffMatches();

        uiModel.addAttribute("stages",playoffMap);
        uiModel.addAttribute("users", userRepository.read());
        uiModel.addAttribute("groups", groupRepository.allGroups());
        List<String> teams = groupRepository.allGroups().stream().flatMap(group -> group.teams.stream()).sorted().collect(toList());
        uiModel.addAttribute("teams", teams);
        uiModel.addAttribute("playoffTreeEditable", (sessionUser.admin || ZonedDateTime.now(Config.STOCKHOLM).isBefore(Config.playoffStart)));

        return "playoff";
    }


    @RequestMapping(method = RequestMethod.POST)
    public String savePlayoff(Model uiModel, HttpServletRequest request) {
        User user = userRepository.read(Long.parseLong(request.getParameter("userId")));
        List<Match> resultList = getResults(request, user);
        matchRepository.store(resultList);
        return playoff(uiModel, request);
    }

    @RequestMapping(value = "/stage",  method = RequestMethod.POST)
    public String createPlayoffStage(Model uiModel,  HttpServletRequest request) {
        String stage = request.getParameter("stage");
        Match.Stage newStage = valueOf(stage);
        switch (newStage){
            case  FINAL : {
                sweepstake.createFinalStage();
                break;
            }
            case  SEMI_FINAL: {
                sweepstake.createSemiFinalStage();
                break;
            }
            case  QUARTER_FINAL: {
                sweepstake.createQuarterFinalStage();
                break;
            }

            case  LAST_SIXTEEN: {
                sweepstake.createLastSixteenStage();
                break;
            }
            default :{

            }
        }

        return playoff(uiModel, request);

    }
}