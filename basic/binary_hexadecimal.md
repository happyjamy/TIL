# 2진수와 16진수

## bit 란?

이진수(Binary Digit)의 줄임말인 비트는 컴퓨팅 및 디지털 통신에서 **가장 작은 데이터 단위**.  
0 또는 1의 두 가지 가능한 상태 중 하나를 나타냄.

1bit의 경우의 수는 **0,1** -> 2가지  
4bit의 경우의 수는 **2^4** -> 16가지

</br>

### **2진수 10진수 변환**

<img src="https://user-images.githubusercontent.com/78072370/232103116-d1f901df-b25d-40e8-8f53-fa2b4800c51e.png" width="400" height="250">

=> 해당 수는 8+1 = 9

</br>

---

</br>

## 16진수

16진수 한자리 수는 4bit 이다!  
0~15까지 한자리로 나타낼 수 있다.

> ex) **0xF4** (0x 같은 prefix를 붙여서 명시적으로 16진수임의 나타냄) => 8비트

</br>

### **2진수 <-> 16진수 변환**

</br>

<img src="https://user-images.githubusercontent.com/78072370/232104342-acdf2423-514f-4b42-a69f-94a1e98b7c24.png" width="400" height="300">

### 16진수 표기가 사용되는 예

- 색상표현 (RGB)
- 컴퓨터 하드웨어 주소 -> 비쥬얼 스튜디오 c언어 메모리 윈도우
- 메모리 값 표현

</br>

## n진수 -> 10진수 변환은 왜 **각자리수\*밑을 더한 값** 일까?

우리는 Positional number system 을 사용하기 때문

**Positional number system?**  
number의 값에 대한 digit의 기여가 그 자릿수의 위치에 따라 결정되는 숫자체계.  
digit의 위치에 따라 해당하는 Radix의 거듭제곱이 결정되고, Number의 값은 각자리마다 결정된 Radix 거듭제곱을 각 digit과 곱해 합산하여 계산함.

<img src="https://user-images.githubusercontent.com/78072370/232185482-94e90dd9-4a9a-4ca0-aa95-bd225872b9e1.png" width="300" height="200">

<br/>

### 🔍 결론

결국 n진법은 0부터 n개의 숫자를 사용해서 수를 **표현**하는 표현법 임!  
우리가 일상생활에서 자주 쓰는 10진수도 결국 "325"라는 Number 실제 값을 나타내기 위해 쓰는 표현일뿐

> 3\*10^2 + 2\*10^1 + 5\*10^0 = 325

연산과정을 거쳐야 실제값이 탄생하는 것임.  
우리는 10진수를 널리 쓰니까 보통 자동으로 포지션을 연산해서 계산함. 하지만 2진수,16진수 같은 것은 자주 접하지 않으니 실제 Number 을 구하기 위해 명시적인 연산이 필요한 편.  
그러므로 n진수 -> 10진수 변환이 **각자리수\*밑을 더한 값** 인 이유는 그게 현대 숫자 시스템에서 실제 값 Number 를 구하는 방법이자 우리가 자동으로 실제값으로 연산해버리는 10진수 표현법이기도 해서이다!

</br>

### 반대의 경우 10진수 -> n진수 는?

결국 해당 원리도 2가지 법칙을 알면됨.

- n진수와 10진수가 표현법만 다를 뿐이지 **"같은 크기를 가진 수"** 이다.  
  ex) 10진수 A를 2로 나누었을때 나머지는 2진수 B를 2로 나누었을때 나머지와 같음.
- n진수 수를 n으로 나누면 소수점이 왼쪽으로 1자리씩 이동함
  -> **1의 자리수가 추출 됨.**

실제 예시  
<img src="https://user-images.githubusercontent.com/78072370/232202590-dd7b7582-113b-4ee5-99a4-6df819a164d0.png" width="430" height="300">

이때 2번에 의해 2진수를 2로 나누면 1의 자리수가 나머지로 추출된다. (현재 나누어지는 수의 LSD 추출)  
즉, 2로 나눌때마다 현재 수의 1의자리수가 추출되고 원래 수는 소수점 왼쪽으로 한칸 이동하니까 -> 처음 추출되는 나머지가 LSD 마지막에 추출되는 나머지가 MSD가 된다.

</br>

---

## 컴퓨터의 단위 체계

Computer = CPU + RAM  
1 byte = 8bit

컴퓨터에서 메모리 용량을 논할때 최소 단위 → 1byte  
컴퓨터는 메모리 관리를 1 byte 단위로 한다.

2^4 = 16  
2^8 = 256  
2^16 = 65536 (65536 byte = 64KB)  
2^10 = 1024 ⇒ 용량변환 기준

64비트 운영체제는 2^64 개 만큼의 메모리 주소를 저장 할 수 있다는 것.

</br>

## 컴퓨터가 글자와 이미지를 다루는 법

컴퓨터의 세계에는 오직 **숫자** 밖에 없다.

그럼 글자를 표현하려면? 숫자와 1:1 매핑시키자! => 이게 **"코드체계"**
미국에서 사용하는 표준 코드체계 = ASCII

그럼 이미지를 표현하려면?  
모니터 상 점 하나를 **화소** 라고 한다. -> 이 화소들을 모아 이미지를 만들 수 있음

- RGV = 24bit
- RGVA = 32bit

1024\*768 해상도의 사진에는 화소가 1024\*768 개 존재한다.  
총 용량은 32 bit = 4byte 기준 1024\*768\*4. 대략 28MB 넘음.  
이렇게 화소 하나하나의 정보를 나열한 것이 -> **bitmap** 형식 => 용량이 너무 큼!  
그래서 우리는 압축을 해서 이미지를 본다. (jpg, png)

</br>

</br>

</br>

Reference  
[10진수 2진수 변환 공식 원리와 정리](https://thrillfighter.tistory.com/519)