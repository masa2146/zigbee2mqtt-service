# IoT Device Management System Technical Documentation

## Overview

Bu sistem, IoT cihazlarının yönetimi, komut gönderimi ve kural tabanlı otomasyon işlemleri için tasarlanmış kapsamlı bir
yönetim sistemidir. Sistem temel olarak dört ana bileşenden oluşmaktadır:

1. Cihaz Yönetimi
2. Cihaz Komutları Yönetimi
3. Cihaz Kuralları Yönetimi
4. Sensör Veri İşleme ve Otomasyon

## Servis Detayları

### 1. Device  Service

`DeviceService`, IoT cihazları yönetmek için kullanılan servistir.

#### Temel Özellikler:

- Cihaz oluşturma
- Cihaz güncelleme
- Cihaz silme
- Cihaz listeleme
- Tekil Cihaz görüntüleme

#### Örnek Kullanım:

// Cihaz Oluşturma (POST /api/v1/devices)

```json
{
  "disabled": false,
  "friendlyName": "motion_sensor_1",
  "modelId": "custom_motion_sensor"
}
```

### 2. Device Command Service

`DeviceCommandService`, cihazlara gönderilebilecek komutların yönetimi ve komut çalıştırma işlemlerini gerçekleştirir.

#### Temel Özellikler:

- Komut şablonu oluşturma
- Komut güncelleme
- Komut silme
- Model bazlı komut listeleme
- Komut çalıştırma
- Önbellekleme mekanizması (Caffeine Cache)

#### Örnek Kullanım:

// Komut Oluşturma (POST /api/v1/commands)

```json
{
  "modelId": "TS0216",
  "commandName": "stop_alarm",
  "commandTemplate": "{\"alarm\":false}",
  "description": "Alarmı durdur"
}
```

```json
{
  "modelId": "TS0216",
  "commandName": "START_alarm",
  "commandTemplate": "{\"alarm\":true}",
  "description": "Alarmı başlat"
}
```

// Komut Çalıştırma (POST /api/v1/commands/execute)

```json
{
  "deviceName": "Siren33",
  "commandName": "start_alarm"
}
```

``Siren33`` adlı cihazın zigbee modeli ``TS0216`` olduğu için bu cihaza ``start_alarm`` komutu gönderilir.

### 3. Device Rule Service

`DeviceRuleService`, cihazlar için otomatik tetikleyici kuralların yönetimini sağlar.

#### Temel Özellikler:

- Kural oluşturma
- Kural güncelleme
- Kural silme
- Kural listeleme
- Gerçek zamanlı kural değerlendirme
- Sıralı olay takibi
- Zaman bazlı koşul kontrolü

#### Örnek Kullanım:

// Kural Oluşturma (POST /api/v1/rules)

```json
{
  "name": "Hareket Algılandığında siren çal",
  "description": "Sensör hareket algıladığında belirtilen sireni çalıştır",
  "condition": {
    "criteria": [
      {
        "deviceName": "motion_sensor_1",
        "field": "motion",
        "operator": "EQUALS",
        "value": true
      }
    ],
    "maxTimeDifferenceMs": 5000,
    "requiredDeviceSequence": [
      "motion_sensor_1"
    ]
  },
  "action": {
    "targetDeviceName": "Siren33",
    "commandName": "start_alarm",
    "parameters": {
      "battery": 0,
      "linkquality": 134,
      "voltage": 3400
    }
  }
}
```

## Önemli Sınıflar ve Veri Yapıları

### DeviceCriteria

Cihaz durumlarını değerlendirmek için kullanılan kriter yapısı.

```json
{
  "deviceId": "sensor_id",
  "field": "temperature",
  "operator": "GREATER_THAN",
  "value": 25
}
```

### ComparisonOperator

Kriter değerlendirmelerinde kullanılan karşılaştırma operatörleri:

- EQUALS
- NOT_EQUALS
- GREATER_THAN
- LESS_THAN
- GREATER_THAN_OR_EQUALS
- LESS_THAN_OR_EQUALS
- CONTAINS
- STARTS_WITH
- ENDS_WITH

### RuleCondition

Kural tetikleyici koşullarını tanımlayan yapı:

- criteria: Değerlendirilecek kriterler listesi
- maxTimeDifferenceMs: Maksimum zaman farkı (ms)
- requiredDeviceSequence: Sıralı cihaz listesi

### RuleAction

Kural tetiklendiğinde gerçekleştirilecek aksiyonu tanımlayan yapı:

- targetDeviceId: Hedef cihaz ID
- commandName: Çalıştırılacak komut
- parameters: Komut parametreleri

## Kompleks Kural Örneği

```json
{
  "name": "Sıcaklık ve Nem Kontrolü",
  "description": "Sıcaklık ve nem belirli değerleri aştığında klimayı çalıştır",
  "condition": {
    "criteria": [
      {
        "deviceId": "temp_sensor_1",
        "field": "temperature",
        "operator": "GREATER_THAN",
        "value": 26
      },
      {
        "deviceId": "humidity_sensor_1",
        "field": "humidity",
        "operator": "GREATER_THAN",
        "value": 65
      }
    ],
    "maxTimeDifferenceMs": 30000,
    "requiredDeviceSequence": [
      "temp_sensor_1",
      "humidity_sensor_1"
    ]
  },
  "action": {
    "targetDeviceId": "ac_unit_1",
    "commandName": "setMode",
    "parameters": {
      "mode": "COOL",
      "temperature": 24,
      "fanSpeed": "AUTO"
    }
  }
}
```
