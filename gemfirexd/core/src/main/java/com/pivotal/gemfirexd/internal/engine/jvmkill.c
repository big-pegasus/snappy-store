/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <sys/types.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <jvmti.h>


static FILE * logFile = NULL;

void logMessage(char *format, ...) {
   va_list args;
   va_start(args, format);
   vfprintf(logFile, format, args);
   fflush(logFile);
   va_end(args);
}

static void JNICALL
resourceExhausted(
      jvmtiEnv *jvmti_env,
      JNIEnv *jni_env,
      jint flags,
      const void *reserved,
      const char *description)
{
   if(logFile == NULL) {
     logFile = fopen("jvmkill.log", "a");
   }
   logMessage("ResourceExhausted: %s: killing current process!", description);
   kill(getpid(), SIGKILL);
}

JNIEXPORT jint JNICALL
Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
   if(logFile == NULL) {
        logFile = fopen("jvmkill.log", "a");
   }

   jvmtiEnv *jvmti;
   jvmtiError err;
   jint rc = (*vm)->GetEnv(vm, (void **) &jvmti, JVMTI_VERSION);
   if (rc != JNI_OK) {
      logMessage("ERROR: GetEnv failed: %d\n", rc);
      return JNI_ERR;
   }

   jvmtiEventCallbacks callbacks;
   memset(&callbacks, 0, sizeof(callbacks));

   callbacks.ResourceExhausted = &resourceExhausted;

   err = (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof(callbacks));
   if (err != JVMTI_ERROR_NONE) {
      logMessage("ERROR: SetEventCallbacks failed: %d\n", err);
      return JNI_ERR;
   }

   err = (*jvmti)->SetEventNotificationMode(
         jvmti, JVMTI_ENABLE, JVMTI_EVENT_RESOURCE_EXHAUSTED, NULL);
   if (err != JVMTI_ERROR_NONE) {
      logMessage("ERROR: SetEventNotificationMode failed: %d\n", err);
      return JNI_ERR;
   }

   return JNI_OK;
}
