package org.mtransit.parser.ca_central_fraser_valley_transit_system_bus;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.regex.Pattern;

// https://www.bctransit.com/open-data
public class CentralFraserValleyTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new CentralFraserValleyTransitSystemBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "CFV TS";
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		if (gRoute.getRouteLongNameOrDefault().contains("FVX")) {
			return EXCLUDE; // available in Fraser Valley Express app
		}
		if (CharUtils.isDigitsOnly(gRoute.getRouteShortName())) {
			final int rsn = Integer.parseInt(gRoute.getRouteShortName());
			if (rsn > 50) {
				return EXCLUDE; // available in Chilliwack app
			}
		}
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return false; // GTFS-RT
	}

	@Override
	public @Nullable String getRouteIdCleanupRegex() {
		return "\\-[A-Z]+$";
	}

	@Override
	public @Nullable Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "FAIR":
			return 1_001L;
		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	// private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		final int rsn = Integer.parseInt(gRoute.getRouteShortName());
		switch (rsn) {
		// @formatter:off
		case 1: return "8CC63F";
		case 2: return "8077B6";
		case 3: return "F8931E";
		case 4: return "AC5C3B";
		case 5: return "A54499";
		case 6: return "00AEEF";
		case 7: return "00AA4F";
		case 9: return "A2BCCF";
		case 12: return "0073AE";
		case 15: return "49176D";
		case 16: return "B3AA7E";
		case 17: return "77AE99";
		case 21: return "7C3F25";
		case 22: return "FFC20E";
		case 23: return "A3BADC";
		case 24: return "ED1D8F";
		case 26: return "F49AC1";
		case 31: return "BF83B9";
		case 32: return "EC1D8D";
		case 33: return "367D0F";
		case 34: return "FFC10E";
		case 35: return "F78B1F";
		case 39: return "0073AD";
		case 40: return "49176D";
		case 66: return "0D4D8B";
		// @formatter:on
		default:
			throw new MTLog.Fatal("Unexpected route color for %s!", gRoute.toStringPlus());
		}
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean allowNonDescriptiveHeadSigns(long routeId) {
		if (routeId == 26L) {
			return true; // 2023-12-26: it's a mess
		}
		return super.allowNonDescriptiveHeadSigns(routeId);
	}

	private static final Pattern BAY_AZ_ = CleanUtils.cleanWords("bay [a-z]");

	@NotNull
	@Override
	public String cleanDirectionHeadsign(int directionId, boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = PARSE_HEAD_SIGN_WITH_DASH_.matcher(directionHeadSign).replaceAll(PARSE_HEAD_SIGN_WITH_DASH_KEEP_TO);
		directionHeadSign = CleanUtils.keepToAndRemoveVia(directionHeadSign);
		directionHeadSign = cleanHeadSign(directionHeadSign);
		directionHeadSign = BAY_AZ_.matcher(directionHeadSign).replaceAll(EMPTY);
		return directionHeadSign;
	}

	private static final Pattern ENDS_WITH_CONNECTOR = Pattern.compile("( connector$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern PARSE_HEAD_SIGN_WITH_DASH_ = Pattern.compile("(^" +
			"([^\\-]+ -)?" + // from?
			"([^\\-]+)" + // to
			"(- ([^\\-]+))?" + // via?
			")", Pattern.CASE_INSENSITIVE);
	private static final String PARSE_HEAD_SIGN_WITH_DASH_KEEP_TO = "$3"; // "from - to - via" <= keep to

	private static final Pattern PARSE_HEAD_SIGN_WITH_DASH_FROM_ = Pattern.compile("(^" +
			"([^\\-]+ -)" + // from
			")", Pattern.CASE_INSENSITIVE);

	private static final Pattern PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_ = Pattern.compile("(^" +
			"([^\\-]+)" + // to
			"(- ([^\\-]+))" + // via
			")", Pattern.CASE_INSENSITIVE);
	private static final String PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_REPLACEMENT = "$2via $4"; // "to - via"-> "to via via"

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = PARSE_HEAD_SIGN_WITH_DASH_FROM_.matcher(tripHeadsign).replaceAll(EMPTY); // remove "from - "
		tripHeadsign = PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_.matcher(tripHeadsign).replaceAll(PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_REPLACEMENT); // "to via via"
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		return cleanHeadSign(tripHeadsign);
	}

	@NotNull
	private String cleanHeadSign(@NotNull String tripHeadsign) {
		tripHeadsign = ENDS_WITH_CONNECTOR.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
