package railview.railmodel.infrastructure.railsys7;

import java.nio.file.Path;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.timetable.service.ITimetableServiceUtility;

public class TimetableReader {
	public static TimetableReader getRailSys7Instance(Path path,
			IInfrastructureServiceUtility infraServiceUtility,
			IRollingStockServiceUtility rollingStockServiceUtility) {
		return new TimetableReader(path, infraServiceUtility, rollingStockServiceUtility);
	}

	private TimetableReader(Path path,
			IInfrastructureServiceUtility infraServiceUtility,
			IRollingStockServiceUtility rollingStockServiceUtility) {
		this.path = path;
		this.infraServiceUtility = infraServiceUtility;
		this.rollingStockServiceUtility = rollingStockServiceUtility;
	}

	public ITimetableServiceUtility initialize(boolean isCoreModel) {
		ITimetableServiceUtility timeTableServiceUtility =
				new railapp.timetable.service.ServiceUtility();
		if (isCoreModel) {
			railapp.parser.coremodel.timetable.TimetableParser railMLParser =
				railapp.parser.coremodel.timetable.TimetableParser.getInstance(
					infraServiceUtility, rollingStockServiceUtility, timeTableServiceUtility, path.toString());
			railMLParser.parse();
		} else {
			railapp.parser.railsys7.timetable.TimetableParser railSysParser =
				railapp.parser.railsys7.timetable.TimetableParser.getInstance(
						path, timeTableServiceUtility, infraServiceUtility, rollingStockServiceUtility);
			railSysParser.parse();
		}

		return timeTableServiceUtility;
	}

	private Path path;
	private IInfrastructureServiceUtility infraServiceUtility;
	private IRollingStockServiceUtility rollingStockServiceUtility;
}
