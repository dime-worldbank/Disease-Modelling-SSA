package uk.ac.ucl.protecs.objects.locations;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.ac.ucl.protecs.objects.hosts.Person;
import uk.ac.ucl.protecs.objects.hosts.Person.SEX;


public class Household extends Location {
	
	HashSet<Person> peopleBelongingToHousehold = new HashSet<Person>();
	
	public Household(String id, Location l){
		super();
		myId = "HH_" + id;
		mySuperLocation = l;
		setLocationType(LocationCategory.HOME);
	}
	
	public void addPersonToHousehold(Person p) {
		peopleBelongingToHousehold.add(p);
	}
	public HashSet<Person> getPeopleBelongingToHousehold(){
		return peopleBelongingToHousehold;
	}
	public void removeDeceasedFromHousehold(Person p) {
		if (!p.isAlive()){
			peopleBelongingToHousehold.remove(p);
		}
	}
	public void determineWaterGathererInHousehold() {
		// only work on households that aren't empty
		if (this.getPeopleBelongingToHousehold().size() > 0) {
			Map<SEX, Map<Boolean, List<Person>>> adultBySexInHousehold = this.getPeopleBelongingToHousehold().stream().collect(
					Collectors.groupingBy(
							Person::getSex,
							Collectors.groupingBy(
									Person::isAdult
									)
							)
					);
			boolean adultInHouse = this.getPeopleBelongingToHousehold().stream().anyMatch(p -> (p.isAdult() & p.isAlive()));
			boolean adultFemaleInHouse = this.getPeopleBelongingToHousehold().stream().anyMatch(p -> (p.getSex().equals(SEX.FEMALE) & p.isAdult() & p.isAlive()));
			// no adults in household just assign the first person as the water gatherer
			if (!adultInHouse) {
				this.getPeopleBelongingToHousehold().iterator().next().setWaterGatherer();
			}
			// adults in the house, get the first adult female and assign them as the water gatherer
			else if (adultFemaleInHouse) {
				List<Person> adultFemales = adultBySexInHousehold.get(SEX.FEMALE).get(true);
				adultFemales.get(0).setWaterGatherer();
			}
			// no adults in the house get the first adult and assign them as the water gatherer
			else {
				List<Person> adultMales = adultBySexInHousehold.get(SEX.MALE).get(true);
				adultMales.get(0).setWaterGatherer();
			}
		}
	}	
}
