import java.text.*;
import java.util.*;

class Distribution{
	int[] len;
	int[] freq;
	Random r;
	int minl;
	public Distribution(int[] x, int[] y){
		// we assume the input distribution is 
		// an ordered array of lists, and it's respective frequencies
		len = x;
		freq = y;
		minl = x[0];
		r = new Random();
	}
	// pick the next element with length at most the upper bound ub
	public int next(int ub){
		int sum = 0;
		for(int i=0;i<len.length;i++){
			if(len[i]<=ub){
				sum+=freq[i];
			}else{
				break;
			}
		}
		int x = r.nextInt(sum);
		sum = 0;
		for(int i=0;i<len.length;i++){
			if(len[i]<=ub){
				sum+=freq[i];
				if(sum>x){
					return len[i];
				}
			}
		}
		return 0;
	}
}
class Interval{
	int s; // how many unit from the start of a marked line
	int l; // length
	public Interval(int ss, int ll){
		s = ss;
		l = ll;
	}
	public ArrayList<Interval> split(int j, int m){
		ArrayList<Interval> a = new ArrayList<Interval>();
		a.add(new Interval(Math.min(s,j),j));
		if(j<=s){
			a.add(new Interval(s-j,l-j));
		}else{
			int t = (j-m*((j-s)/m)-s);
			a.add(new Interval((m-t) % m,l-j));
		}
		return a;
	}
	
	public String toString(){
		return s+" "+l;
	}
}
class Street{
	Distribution dist;
	int len;
	int mark;
	int offset;
	double good;
	boolean hittheline;
	Random r;
	public Street(Distribution distribution, int length, int m, int o){
		len = length;
		mark = m;
		dist = distribution;
		offset = o;
		r = new Random();
	}
	public double simulate(int iterations){
		int sum=0;
		for(int i=0;i<iterations;i++){
			sum+=simulate();
		}
		return ((double) sum)/((double) iterations);
	}
	public void split(int i, int j, int car, ArrayList<Interval> street){
		Interval I = street.get(i);
		ArrayList<Interval> e = I.split(j, mark);
		ArrayList<Interval> f = e.get(1).split(car, mark);
		Interval I1 = e.get(0);
		Interval I2 = f.get(1);
		street.remove(i);
		if(I1.l>=dist.minl){
			street.add(I1);
		}
		if(I2.l>=dist.minl){
			street.add(I2);
		}
	}
	public int simulate(){
		ArrayList<Interval> space = new ArrayList<Interval>();
		space.add(new Interval(offset,len));
		int ub=len;
		int count = 0;
		boolean moreline=true;
		while(!space.isEmpty()){
			ub = 0;
			for(int i=0;i<space.size();i++){
				ub = Math.max(ub, space.get(i).l);
			}
			int car = dist.next(ub);
			// now we want to place this car somewhere. 
			if(car==0){
				return count;
			}
			count++;
			//System.out.println(space);
			if(r.nextDouble()<good){
				// we got a good driver
				if(hittheline && moreline){
					// hit the line if there are more lines
					moreline = hit_the_line(space,car);
				}
				if((!moreline) || !hittheline){
					// kiss the bumper
					kiss_the_bumper(space, car);
				}
			}else{
				// bad driver park randomly
				random_park(space,car);
			}
		}
		return count;
	}
	public boolean hit_the_line(ArrayList<Interval> space, int car){
		int positions = 0;
			// hit the line
			// the number of positions where one can hit the line
			for(int i=0;i<space.size();i++){
				//System.out.println(space.get(i).l+" "+car+" "+space.get(i).s);
				int length = space.get(i).l - car - space.get(i).s;
				if(length>0){
					positions += length/mark+1;
				}
			}
			if(positions>0){
				int place = r.nextInt(positions);
				positions=-1;
				for(int i=0;i<space.size();i++){
					int length = space.get(i).l - car - space.get(i).s;
					if(length>0){
						positions += length/mark+1;
					}
					if(positions>=place){
						// splitting
						//System.out.println(space+" "+i+" "+space.get(i).s+" "+positions+" "+place+" "+car);
						split(i, space.get(i).s+(positions-place)*mark, car, space);
						//System.out.println(space);
						return true;
					}
				}
			}
			return false;
		}
	public void kiss_the_bumper(ArrayList<Interval> space, int car){
		
		// There are two interpretations
		
		int positions=0;
		
		// the probability of going into one interval
		// is proportional to the length of the interval
		
		for(int i=0;i<space.size();i++){
			positions += Math.max(space.get(i).l - car + 1,0);
		}
		int place = r.nextInt(positions);
		positions = -1;
		for(int i=0;i<space.size();i++){
			positions += Math.max(space.get(i).l - car + 1,0);
			if(positions>=place){
				split(i, 0, car, space);
				return;
			}
		}
	}
	public void random_park(ArrayList<Interval> space, int car){
		// bad driver park randomly
		int positions = 0;
		for(int i=0;i<space.size();i++){
			positions += Math.max(space.get(i).l - car + 1,0);
		}
		int place = r.nextInt(positions);
		positions = -1;
		for(int i=0;i<space.size();i++){
			positions += Math.max(space.get(i).l - car + 1,0);
			if(positions>=place){
				split(i,positions-place,car,space);
				return;
			}
		}
	}
}
public class Parking{
	static Interval interval;
	public static void main(String[] args){
		NumberFormat formatter = new DecimalFormat("#0.00");     
		int foot = 12;   // how many inches in a foot.
		int inch = 1000; // How many unit does a inch equal to
		// a unit is the smallest length the simulation can differentiate
		
		int[] len = {158,162,167,169,170,172,173,174,175,176,177,178,179,180,
				     181,182,183,184,185,186,187,188,189,190,191,192,193,194,
				     195,196,197,198,200,201,202,203,204,205,206,207,208,212,
				     213,215,219,221,222,224,225,228,232,240};// car size in inches
		int[] freq = {4559,4842,444,6555-5914+5914,2779,1307,6132,4478,36683,11183,17680,
				      9825,24060,20829,3777,1055,7062,14114,4302,1806,9823,21029,
				      26811,31892,17631,23805,3444,110,23617,233+5914-5914,8392,2627,15669,
				      9608,9439,20983,5423,6368,1955,3377,4141,1536,81,777,994,
				      3970,3537,3264,1492,9957,27630,30043}; // frequency
		
	
		for(int i=0;i<len.length;i++){
			len[i]+=foot; // 1 foot gap between each car
		}
		
		//int[] len = {feet2inch};
		//int[] freq = {1};

		
		for(int i=0;i<len.length;i++){
			len[i]*=inch;
		}
		
		Distribution d = new Distribution(len,freq);
		//int mark = 385*inch;
		//int l = 340*foot*inch;
		
		
		//int l=3*mark;
		/*
		for(int l=50; l<200; l+=1){
			for(int mark=158;mark<400;mark++){
				Street s = new Street(d,l*foot*inch,mark*inch);
				for(double t=0;t<1.01;t+=0.02){
					s.good = t;
					s.hittheline = true;
					//s.hittheline = false;
					double hit = s.simulate(10000);
					// System.out.println(hit+",");
					
					System.out.println("{"+l+","+ mark+","+formatter.format(t)+","+formatter.format(hit)+"},");
					//System.out.println("{" + (l/foot/inch)  +"  , "+ formatter.format(t)+"   , "+ hit+"},");
				}
			}
		}*/
		
		
		// Find optimal gap
		/*for(int l=30; l<=100; l+=1){
			double max = 0;
			int maxmark = 0;
			int maxoffset = 158;
			for(int offset=158;offset<=252;offset++){
				for(int mark=158;mark<500;mark++){
					Street s = new Street(d,l*foot*inch,mark*inch,offset*inch);
					s.good = 1;
					s.hittheline = true;
					double hit = s.simulate(1000);
					if(hit>max){
						max = hit;
						maxmark = mark;
						maxoffset = offset;
					}
				}
			}
			System.out.println("{"+l+","+maxmark+","+maxoffset+"},");
		}*/
		
		/*int l = 50;
		
		int offset = 170;
		for(int mark=150;mark<=500;mark++){
			Street s = new Street(d,l*foot*inch,mark*inch,offset*inch);
			s.good = 1;
			s.hittheline = true;
			double hit = s.simulate(10000);
			System.out.println("{"+mark+","+hit+"},");
		}*/
		
		//int l = 831*feet2inch*inch;
		int l = 100*foot*inch;
		int mark = 422*inch;
		Street s = new Street(d,l,mark,170);
		for(double p=0;p<1.02;p+=0.02){
		//for(double p=0.5;p<=0.5;p+=0.02){
			s.good = p;
			s.hittheline = true;
			//s.hittheline = false;
			double hit = s.simulate(10000);
			System.out.println(hit+",");
		}
		/*for(int m=300;m<=500;m+=1){
			s.good = 0.75;
			s.hittheline = true;
			s.mark = m*inch;
			//s.hittheline = false;
			double hit = s.simulate(10000);

			System.out.println(hit+",");
		}*/
	}
}
