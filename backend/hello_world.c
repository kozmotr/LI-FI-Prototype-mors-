#include <stdio.h>
#include <sys/select.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
	char* name;
	int age;
} insan;

#define BLINK_TIME  0.1

#define WAIT_TIME 0.05
#define A "SLL"


#define STARTMSG "ledctl ledoff wlan red && ledctl ledoff wlan2 green && ledctl ledon power"
#define SHORTMSG "ledctl ledoff power && ledctl ledoff wlan2 green && ledctl ledon wlan red"
#define LONGMSG "ledctl ledoff power && ledctl ledoff wlan red && ledctl ledon wlan2 green"
#define STOPMSG "ledctl ledoff power && ledctl ledon wlan red && ledctl ledon wlan2 green"
#define PREAMBLEMSG "ledctl ledon power && ledctl ledon wlan red && ledctl ledon wlan2 green"

#define PREAMBLE_OFF "ledctl ledoff power && ledctl ledoff wlan red && ledctl ledoff wlan2 green"
#define STOP_OFF "ledctl ledoff wlan red && ledctl ledoff wlan2 green"

#define WLAN_RED_ON "ledctl ledon wlan red"
#define WLAN_RED_OFF "ledctl ledoff wlan red"
#define WLAN_GREEN_ON "ledctl ledon wlan2 green"
#define WLAN_GREEN_OFF "ledctl ledoff wlan2 green"
#define POWER_ON  "ledctl ledon power"
#define POWER_OFF  "ledctl ledoff power"

#define WAIT_NANO(sec) do { \
	struct timespec mytiv;\
	struct timespec myf;\
	mytiv.tv_sec = 0;\
	mytiv.tv_nsec = sec * 1000000000;\
	nanosleep(&mytiv,&myf);\
	}while(0)

/*
void wait_nano(double sec)
{
	struct timespec mytiv;
	struct timespec myf;
	mytiv.tv_sec = 0;
	mytiv.tv_nsec = sec * 1000000000;
	nanosleep(&mytiv,&myf);

}
*/

void  wait(double sec){
	struct timeval tim;
	tim.tv_sec = 0;
	tim.tv_usec = 1000000*sec;

	select(0,0,0,0,&tim);

}



#define OPEN_WLAN_RED(sec) do{\
	system(WLAN_RED_ON);\
	WAIT_NANO((double)sec/4);\
	system(WLAN_RED_OFF);\
	WAIT_NANO((double)sec * 3/4);\
	}while(0);

#define OPEN_WLAN_GREEN(sec) do{\
	system(WLAN_GREEN_ON);\
	WAIT_NANO((double)sec/3);\
	system(WLAN_GREEN_OFF);\
	WAIT_NANO((double)sec * 2/3);\
	}while(0);




#define S do{\
	system(SHORTMSG);\
	WAIT_NANO((double)WAIT_TIME/3);\
	system(WLAN_RED_OFF);\
	WAIT_NANO((double)WAIT_TIME* 2/3);\
	}while(0);



#define L do{\
	system(LONGMSG);\
	WAIT_NANO((double)WAIT_TIME/3);\
	system(WLAN_GREEN_OFF);\
	WAIT_NANO((double)WAIT_TIME* 2/3);\
	}while(0);

#define STOP(sec) do{\
	system(STOPMSG);\
	WAIT_NANO((double)sec/3);\
	system(STOP_OFF);\
	WAIT_NANO((double)sec* 2/3);\
	}while(0);

#define START(sec) do{\
	system(STARTMSG);\
	WAIT_NANO((double)sec/3);\
	system(POWER_OFF);\
	WAIT_NANO((double)sec* 2/3);\
	}while(0);

#define PREAMBLE(sec) do{\
	system(PREAMBLEMSG);\
	WAIT_NANO((double)sec/3);\
	system(PREAMBLE_OFF);\
	WAIT_NANO((double)sec* 2/3);\
	}while(0);



#define m_open_led(led_name , color , sec) do { \
		char *open_command ;\
		open_command = (char*) malloc(100*sizeof(char));\
		sprintf(open_command , "ledctl ledon %s %s" , led_name,color);\
		free(open_command);\
		wait_nano((double)(sec));\
		}while(0);







void open_led(char* led_name , char* color ,double sec)
{
	char *open_command ;
	open_command = (char*) malloc(100*sizeof(char));
	sprintf(open_command , "ledctl ledon %s %s" , led_name,color);
	system(open_command);
	free(open_command);
	wait((double)(sec));

}



#define m_close_led(led_name , color , sec) do { \
		char *close_command ;\
		close_command = (char*) malloc(100*sizeof(char));\
		sprintf(close_command , "ledctl ledoff %s %s" , led_name,color);\
		free(close_command);\
		wait_nano((double)(sec));\
		}while(0);


void close_led(char* led_name , char* color,double sec)
{
	char *close_command ;
	close_command = (char*) malloc(100*sizeof(char));
	sprintf(close_command , "ledctl ledoff %s %s" , led_name,color);
	system(close_command);
	free(close_command);
	wait(sec);

}



#define DEBUG 1

#define CASE(token,symbols) do {\
	if(#token[0]==c){ \
		if (DEBUG) printf("%s\n",#symbols);\
		symbols}\
	}while(0);


void encode_char(char c)
{
	CASE(a , S L)
	CASE(b, L S S S)
	CASE(c , L S L S)
	CASE(d , L S S)
	CASE(e,S)
	CASE(f, S S L S)
	CASE(g , L L S)
	CASE(h , S S S S)
	CASE(i, S S)
	CASE(j , S L L L)
	CASE(k , L S L)
	CASE(l , S L S S )
	CASE(m , L L )
	CASE(n , L S )
	CASE(o , L L L)
	CASE(p , S L L S)
	CASE(q, L L S L)
	CASE(r , S L S )
	CASE(s , S S S)
	CASE(t , S)
	CASE(u , S S L)
	CASE(v , S S S L)
	CASE(w , S L L)
	CASE(x , L S S L)
	CASE(y, L S L L)
	CASE(z, L L S S)
}

void send_char(char c)
{
	printf("START\n");
	START(WAIT_TIME);
	encode_char(c);
	STOP(WAIT_TIME);
	printf("STOP\n");
}

void encode_string(char* string_to_send)
{
	int len = strlen(string_to_send);
	int i;
	for( i = 0 ; i < len ; i++)
	{
		send_char(string_to_send[i]);
	}
}

void start_send_string(char* string_to_send)
{
	PREAMBLE(WAIT_TIME);
	encode_string(string_to_send);
	PREAMBLE(WAIT_TIME);
}



int main(int argc , char**argv)
{

	int total_blink = 0;
	char * string_to_send ;
	char * color ;
	int total_time  = 0;
	if (argc < 2)
	{
		printf("Usage of progrfam is ./program string_to_send");
		exit(-1);
	}
	else
	{
		string_to_send = argv[1];
	}

	start_send_string(string_to_send);
	return 0;


	double wait_time = (double) total_time / total_blink;


	int blinked_count = 0;

	while (blinked_count < total_blink)
	{

		OPEN_WLAN_GREEN(wait_time);
		blinked_count ++ ;


	}



	return 0;
}
