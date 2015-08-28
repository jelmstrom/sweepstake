package com.jelmstrom.tips;


import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping(value = "/group")
public class GroupController extends BaseController {

    public GroupController() {
        super();
    }
    public String groupViewModel(Model uiModel, HttpServletRequest request, long groupID) {
        List<TableEntry> tableEntries = sweepstake.currentStandingsForGroup(groupID);
        List<TablePrediction> predictions = sweepstake.getPredictions(sessionUserId(request));
        Optional<TablePrediction> maybe = predictions.stream().filter(entry -> entry.group.equals(groupID)).findFirst();
        List<Match> groupMatches = matchRepository.groupMatches(groupID);
        Collections.sort(groupMatches);
        setSessionUsers(request, uiModel);
        uiModel.addAttribute("matches", groupMatches);
        uiModel.addAttribute("groups", groupRepository.allGroups());
        uiModel.addAttribute("group",groupRepository.read(groupID));
        uiModel.addAttribute("currentStandings", tableEntries);
        uiModel.addAttribute("teams", tableEntries.stream().map(entry -> entry.team).collect(toList()));
        uiModel.addAttribute("prediction", maybe.orElse(TablePrediction.emptyPrediction()));
        return "group";
    }



    @RequestMapping(value = "/{groupId}", method = RequestMethod.GET)
    public String showGroup(Model uiModel, @PathVariable String groupId, HttpServletRequest request) {
        long groupID = Long.parseLong(groupId);
        return groupViewModel(uiModel, request, groupID);
    }

    @RequestMapping(value = "/{groupId}/add", method = RequestMethod.POST)
    public String addTeamToGroup(Model uiModel, @PathVariable String groupId, HttpServletRequest request){
        Group group = groupRepository.read(Long.parseLong(groupId));
        group.teams.add(request.getParameter("newTeam"));
        groupRepository.store(group);
        return showGroup(uiModel, groupId, request);
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String createGroup(Model uiModel, HttpServletRequest request){
        Group group = new Group("new", Collections.emptyList());
        group = groupRepository.store(group);
        return groupViewModel(uiModel, request, group.getGroupId());
    }

    @RequestMapping(value = "/{groupId}/match", method = RequestMethod.POST)
    public String addMatchToGroup(Model uiModel, @PathVariable String groupId, HttpServletRequest request){

        long group = Long.parseLong(groupId);
        String dateString = request.getParameter("matchDate");
        try {
            ZonedDateTime startTime = Config.getZonedDateTime(dateString);
            Match newMatch = new Match(request.getParameter("homeTeam")
                    , request.getParameter("awayTeam")
                    , startTime
                    , group);
            matchRepository.store(newMatch);
        } catch (Exception e) {
            uiModel.addAttribute("dateFormatError", dateString + " invalid. Use yyyy-MM-ddTHH:mm or use Chrome");
        }

        return groupViewModel(uiModel, request, group);
    }

    // GET because form inside form creates problems
    @RequestMapping(value = "/match/{matchId}/delete", method = RequestMethod.GET)
    public String deleteGroupMatch(Model uiModel, @PathVariable String matchId, HttpServletRequest request){
        System.out.printf(" Delete match %s \n", matchId );
        Long match = Long.parseLong(matchId);
        Long groupId = matchRepository.read(match).groupId;
        matchRepository.drop(match);
        return showGroup(uiModel, groupId.toString(), request);
    }

    @RequestMapping(value = "/{groupId}/drop/{team}", method = RequestMethod.POST)
    public String dropTeamFromGroup(Model uiModel, @PathVariable String groupId, @PathVariable String team , HttpServletRequest request){
        Group group = groupRepository.read(Long.parseLong(groupId));
        group.teams.remove(team);
        groupRepository.store(group);
        return showGroup(uiModel, groupId, request);
    }

    @RequestMapping(value = "/{groupId}/name", method = RequestMethod.POST)
    public String updateGroupName(Model uiModel, @PathVariable String groupId, HttpServletRequest request){
        Group group = groupRepository.read(Long.parseLong(groupId));
        Group updated = new Group(request.getParameter("groupName"), group.teams);
        updated.setGroupId(group.getGroupId());
        groupRepository.store(updated);
        return showGroup(uiModel, groupId, request);
    }


    @RequestMapping(value = "/prediction/{groupId}", method = RequestMethod.POST)
    public String storePrediction(Model uiModel, @PathVariable String groupId, HttpServletRequest request) {
        String pos1 = request.getParameter("prediction1");
        String pos2 = request.getParameter("prediction2");
        String pos3 = request.getParameter("prediction3");
        String pos4 = request.getParameter("prediction4");
        System.out.println("store prediction");
        User user = userRepository.read(sessionUserId(request));
        sweepstake.saveUserPrediction(new TablePrediction(Long.parseLong(groupId), user.id, Arrays.asList(pos1, pos2, pos3, pos4)));

        return showGroup(uiModel, groupId, request);
    }


    @RequestMapping(value = "/results/{groupLetter}", method = RequestMethod.POST)
    public String storeGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        User user = userRepository.read(sessionUserId(request));

        List<Match> resultList = getResults(request, user);

        matchRepository.store(resultList);

        return showGroup(uiModel, groupLetter, request);

    }




}